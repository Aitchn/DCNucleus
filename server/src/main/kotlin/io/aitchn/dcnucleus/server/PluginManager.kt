package io.aitchn.dcnucleus.server

import io.aitchn.dcnucleus.api.DCNucleusPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

object PluginManager {
    private val logger: Logger = LoggerFactory.getLogger("[PluginManager]")

    private lateinit var loader: PluginLoader
    private val loaded = ConcurrentHashMap<String, LoadedPlugin>()

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

        logger.info("Plugin '$name' disabled.")
        return true
    }

    fun loadedPlugins(): Set<String> = loaded.keys

    /** 查詢可用（已掃描到但未啟用）的插件名 */
    fun availablePlugins(): Set<String> =
        loader.pluginJarMap.keys - loaded.keys
}