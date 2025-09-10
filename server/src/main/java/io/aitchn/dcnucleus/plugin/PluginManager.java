package io.aitchn.dcnucleus.plugin;

import io.aitchn.dcnucleus.DCNucleus;
import io.aitchn.dcnucleus.api.Plugin;
import io.aitchn.dcnucleus.api.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

public class PluginManager {
    private static final PluginManager instance = new PluginManager();
    private final Logger logger = DCNucleus.logger;

    private final Map<String, LoadedPlugin> loaded = new ConcurrentHashMap<>();
    private final Map<String, String> knownJarSig = new ConcurrentHashMap<>();
    private final Set<String> protectedJars = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final PluginLoader loader;

    private PluginManager() {
        this.loader = new PluginLoader(Path.of("plugins"));
    }

    public static PluginManager getInstance() {
        return instance;
    }

    /**
     * 啟用指定插件
     */
    public boolean enable(String name) {
        if (loaded.containsKey(name)) {
            logger.warn("Plugin {} is already enabled", name);
            return false;
        }

        Path jarPath = loader.getPluginJarMap().get(name);
        if (jarPath == null) {
            logger.error("Plugin {} not found in plugin directory", name);
            return false;
        }

        try {
            if (!isJarFileValid(jarPath)) {
                logger.error("Plugin {} jar file is invalid", name);
                return false;
            }

            PluginDescriptor descriptor = loader.readPluginYaml(jarPath);

            URL[] urls = { jarPath.toUri().toURL() };
            PluginClassLoader classLoader = new PluginClassLoader(urls, getClass().getClassLoader());

            Class<?> mainClass = classLoader.loadClass(descriptor.getEntrypoint());
            Object instanceObj = mainClass.getDeclaredConstructor().newInstance();
            if (!(instanceObj instanceof Plugin instance)) {
                logger.error("Plugin {} entrypoint {} does not extend DCNucleusPlugin",
                        name, descriptor.getEntrypoint());
                return false;
            }

            File dataFolder = new File("plugins", descriptor.getName());
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                logger.warn("Failed to create data folder for plugin {}", descriptor.getName());
            }

            // 注入
            Logger pluginLogger = LoggerFactory.getLogger("[" + descriptor.getName() + "]");
            injectFields(instance, descriptor.getName(), pluginLogger, dataFolder);

            instance.onEnable();
            logger.info("Enabled plugin {} v{}", descriptor.getName(), descriptor.getVersion());

            LoadedPlugin loadedPlugin = new LoadedPlugin(descriptor, instance, classLoader);
            loaded.put(descriptor.getName(), loadedPlugin);
            return true;

        } catch (Exception e) {
            logger.error("Failed to enable plugin {}", name, e);
            return false;
        }
    }

    private void injectFields(Object plugin, String name, Logger logger, File dataFolder) {
        Class<?> currentClass = plugin.getClass();

        while (currentClass != null) {
            for (var field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    try {
                        if (field.getType().equals(String.class) && "name".equals(field.getName())) {
                            field.set(plugin, name);
                            this.logger.debug("Injected name '{}' for plugin {}", name, name);
                        } else if (field.getType().equals(Logger.class) && "logger".equals(field.getName())) {
                            field.set(plugin, logger);
                            this.logger.debug("Injected logger for plugin {}", name);
                        } else if (field.getType().equals(File.class) && "dataFolder".equals(field.getName())) {
                            field.set(plugin, dataFolder);
                            this.logger.debug("Injected dataFolder '{}' for plugin {}", dataFolder, name);
                        }
                    } catch (IllegalAccessException e) {
                        this.logger.error("Failed to inject field {} in plugin {}", field.getName(), name, e);
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }


    /**
     * 停用指定插件
     */
    public boolean disable(String name) {
        LoadedPlugin loadedPlugin = loaded.get(name);
        if (loadedPlugin == null) {
            logger.warn("Plugin {} is not enabled", name);
            return false;
        }

        try {
            // 呼叫 onDisable
            Plugin instance = loadedPlugin.getInstance();
            instance.onDisable();
            logger.info("Disabled plugin {}", name);

            // 關閉 classloader
            try {
                loadedPlugin.getClassLoader().close();
            } catch (Exception e) {
                logger.warn("Failed to close classloader for plugin {}", name, e);
            }

            // 移除 from loaded
            loaded.remove(name);
            return true;

        } catch (Exception e) {
            logger.error("Error disabling plugin {}", name, e);
            return false;
        }
    }

    /**
     * 啟用所有插件
     */
    public void enableAll() {
        loader.refresh().forEach((name, path) -> {
            if (!loaded.containsKey(name)) {
                enable(name);
            }
        });
    }

    /**
     * 停用所有插件
     */
    public void disableAll() {
        new ArrayList<>(loaded.keySet()).forEach(this::disable);
    }

    /**
     * 重新整理插件狀態（檢查 jar 變更）
     */
    public void refresh() {
        Map<String, Path> current = loader.refresh();

        // 檢查刪除的插件
        Set<String> removed = new HashSet<>(loaded.keySet());
        removed.removeAll(current.keySet());
        for (String name : removed) {
            if (isPluginProtected(name)) {
                logger.info("Plugin {} is protected, skip unloading", name);
                continue;
            }
            disable(name);
        }

        // 檢查新增或修改的插件
        for (Map.Entry<String, Path> entry : current.entrySet()) {
            String name = entry.getKey();
            Path path = entry.getValue();
            String sig = computeJarSignature(path);

            // 如果沒看過 → 新插件
            if (!knownJarSig.containsKey(name)) {
                logger.info("Discovered new plugin {}", name);
                enable(name);
                knownJarSig.put(name, sig);
            }
            // 如果雜湊不同 → 代表 jar 更新
            else if (!Objects.equals(knownJarSig.get(name), sig)) {
                logger.info("Detected updated plugin {}", name);
                disable(name);
                enable(name);
                knownJarSig.put(name, sig);
            }
        }
    }

    private String computeJarSignature(Path jar) {
        try (InputStream is = Files.newInputStream(jar)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.warn("Failed to compute signature for {}", jar, e);
            return "";
        }
    }

    /**
     * 檢查 jar 是否有效
     */
    public boolean isJarFileValid(Path jar) {
        File file = jar.toFile();

        if (!file.exists() || !file.isFile() || !file.canRead()) {
            logger.warn("Plugin jar {} is not a valid file", jar);
            return false;
        }

        // 檢查 jar 是否包含 plugin.yml
        try (JarFile jarFile = new JarFile(file)) {
            if (jarFile.getEntry("plugin.yml") == null) {
                logger.warn("Plugin jar {} does not contain plugin.yml", jar);
                return false;
            }
        } catch (IOException e) {
            logger.error("Failed to open plugin jar {}", jar, e);
            return false;
        }

        return true;
    }

    public Map<String, LoadedPlugin> getLoadedPlugins() {
        return Collections.unmodifiableMap(loaded);
    }

    public void setPluginProtected(String name, boolean protect) {
        if (protect) protectedJars.add(name);
        else protectedJars.remove(name);
    }

    public boolean isPluginProtected(String name) {
        return protectedJars.contains(name);
    }
}
