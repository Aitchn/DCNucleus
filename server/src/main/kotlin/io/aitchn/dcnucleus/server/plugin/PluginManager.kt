package io.aitchn.dcnucleus.server.plugin

import io.aitchn.dcnucleus.api.DCNucleusPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile

object PluginManager {
    private val logger: Logger = LoggerFactory.getLogger("[PluginManager]")

    private lateinit var loader: PluginLoader
    private val loaded = ConcurrentHashMap<String, LoadedPlugin>()
    private val knownJarSig = ConcurrentHashMap<String, String>()
    private val protectedJars = ConcurrentHashMap<String, Boolean>()


    fun configure(loader: PluginLoader) {
        this.loader = loader
    }

    fun enable(name: String): Boolean {
        if (loaded.containsKey(name)) {
            logger.warn("Plugin '$name' is already enabled.")
            return false
        }

        val jar = loader.pluginJarMap[name] ?: run {
            logger.error("Plugin '$name' not found.")
            return false
        }

        val yaml = loader.readPluginYaml(jar) ?: return false
        val desc = loader.parseDescriptor(yaml, jar.fileName.toString()) ?: return false

        val cl = PluginClassLoader(jar, DCNucleusPlugin::class.java.classLoader)
        val clazz = Class.forName(desc.entrypoint, true, cl)
        val instance = clazz.getDeclaredConstructor().newInstance() as? DCNucleusPlugin
            ?: run {
                logger.error("Entrypoint ${desc.entrypoint} is not a DCNucleusPlugin")
                cl.close(); return false
            }

        instance.__inject(desc.name)
        instance.onEnable()

        loaded[name] = LoadedPlugin(desc.name, jar, cl, instance)
        knownJarSig[name] = jarSignature(jar)
        logger.info("Plugin '$name' enabled.")
        return true
    }

    fun disable(name: String): Boolean {
        val lp = loaded.remove(name) ?: run {
            logger.warn("Plugin '$name' is not enabled.")
            return false
        }

        runCatching { lp.instance.onDisable() }
            .onFailure { e -> logger.error("onDisable() failed for '$name'", e) }
        runCatching { lp.classLoader.close() }
            .onFailure { e -> logger.warn("Failed to close classloader for '$name'", e) }

        knownJarSig.remove(name)
        logger.info("Plugin '$name' disabled.")
        return true
    }

    fun enableAll(): Int {
        val candidates: Set<String> = loader.pluginJarMap.keys - loaded.keys
        var count = 0
        for (name in candidates) {
            if (enable(name)) count++
        }
        logger.info("Enabled $count/${candidates.size} plugin(s).")
        return count
    }

    fun disableAll(): Int {
        val names = loaded.keys.toList()
        var count = 0
        for (name in names) {
            if (disable(name)) count++
        }
        logger.info("Disabled $count/${names.size} plugin(s).")
        return count
    }

    private fun jarSignature(path: Path): String {
        val size = try { Files.size(path) } catch (_: Exception) { -1L }
        val lm   = try { Files.getLastModifiedTime(path).toMillis() } catch (_: Exception) { -1L }
        return "$size:$lm"
    }

    fun loadedPlugins(): Set<String> = loaded.keys

    /** 查詢可用（已掃描到但未啟用）的插件名 */
    fun availablePlugins(): Set<String> =
        loader.pluginJarMap.keys - loaded.keys

    fun refresh(): Triple<Int, Int, Int> {
        val before = loader.pluginJarMap.toMap()
        val after  = loader.refresh()

        val loadedNow = loaded.keys.toSet()
        var added = 0; var removed = 0; var reloaded = 0

        // 1) 檢查已啟用插件的檔案完整性
        val corruptedPlugins = mutableSetOf<String>()
        for (name in loadedNow) {
            val jarPath = before[name] ?: continue
            if (!Files.exists(jarPath) || !isJarFileValid(jarPath)) {
                logger.error("Plugin '$name' JAR file is corrupted or missing: $jarPath")
                corruptedPlugins.add(name)
            }
        }

        // 強制停用損壞的插件
        for (name in corruptedPlugins) {
            logger.warn("Force disabling corrupted plugin: $name")
            if (disable(name)) removed++
        }

        // 2) 安全的熱重載檢查
        val common = (loadedNow - corruptedPlugins) intersect after.keys
        for (name in common) {
            val newJar = after[name]!!
            val newSig = jarSignature(newJar)
            val oldSig = knownJarSig[name]

            if (oldSig != null && oldSig != newSig) {
                // 檢查新檔案是否有效
                if (!isJarFileValid(newJar)) {
                    logger.error("New JAR for plugin '$name' is invalid, skipping reload")
                    continue
                }

                logger.info("Detected update for plugin '$name', performing hot reload...")
                if (disable(name) && enable(name)) {
                    reloaded++
                }
            }
        }

        // ... rest of the method

        return Triple(added, removed, reloaded)
    }

    /**
     * 檢查 JAR 檔案是否有效且完整
     */
    private fun isJarFileValid(jarPath: Path): Boolean {
        if (!Files.exists(jarPath)) return false

        return try {
            JarFile(jarPath.toFile()).use { jar ->
                // 檢查是否有 plugin.yml
                jar.getJarEntry("plugin.yml") != null
            }
        } catch (e: Exception) {
            logger.debug("JAR validation failed for {}", jarPath, e)
            false
        }
    }

    /**
     * 設置插件保護狀態（防止意外刪除）
     */
    fun setPluginProtected(name: String, protected: Boolean) {
        if (protected) {
            protectedJars[name] = true
            logger.info("Plugin '$name' is now protected from deletion")
        } else {
            protectedJars.remove(name)
            logger.info("Plugin '$name' protection removed")
        }
    }

    /**
     * 檢查插件是否受保護
     */
    fun isPluginProtected(name: String): Boolean = protectedJars.containsKey(name)
}