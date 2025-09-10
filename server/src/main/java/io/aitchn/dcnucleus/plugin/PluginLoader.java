package io.aitchn.dcnucleus.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.aitchn.dcnucleus.DCNucleus;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

public class PluginLoader {
    private final Path pluginDir;
    private final Map<String, Path> pluginJarMap = new HashMap<>();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final Logger logger = DCNucleus.logger;

    public PluginLoader(Path pluginDir) {
        if (!Files.exists(pluginDir)) {
            try {
                Files.createDirectories(pluginDir);
            } catch (IOException e) {
                DCNucleus.logger.error("Failed to create plugin directory", e);
            }
        }
        this.pluginDir = pluginDir;
    }

    /**
     * 掃描 plugins 資料夾並刷新 pluginJarMap
     */
    public Map<String, Path> refresh() {
        pluginJarMap.clear();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pluginDir, "*.jar")) {
            for (Path jar : stream) {
                try {
                    PluginDescriptor descriptor = readPluginYaml(jar);
                    if (!pluginJarMap.containsKey(descriptor.getName())) {
                        pluginJarMap.put(descriptor.getName(), jar);
                    } else {
                        logger.warn("Duplicate plugin name detected: {}", descriptor.getName());
                    }
                } catch (Exception e) {
                    logger.error("Invalid plugin jar: {} ({})", jar.getFileName(), e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to scan plugin directory", e);
        }

        return Collections.unmodifiableMap(pluginJarMap);
    }

    /**
     * 從 jar 檔讀取 plugin.yml
     */
    PluginDescriptor readPluginYaml(Path jar) throws IOException {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            java.util.zip.ZipEntry entry = jarFile.getEntry("plugin.yml");
            if (entry == null) {
                throw new IOException("plugin.yml not found in " + jar);
            }
            try (InputStream is = jarFile.getInputStream(entry)) {
                return parseDescriptor(is);
            }
        }
    }

    /**
     * 解析 plugin.yml 成 PluginDescriptor
     */
    private PluginDescriptor parseDescriptor(InputStream input) throws IOException {
        return yamlMapper.readValue(input, PluginDescriptor.class);
    }

    public Map<String, Path> getPluginJarMap() {
        return pluginJarMap;
    }
}
