package io.aitchn.dcnucleus.server

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile

class PluginLoader(
    private val pluginsDir: Path
) {
    private val logger: Logger = LoggerFactory.getLogger("[PluginLoader]")

    private val _pluginJarMap: MutableMap<String, Path> = LinkedHashMap()
    val pluginJarMap: Map<String, Path> get() = _pluginJarMap

    init {
        if (Files.notExists(pluginsDir)) {
            Files.createDirectories(pluginsDir)
            logger.info("Created plugins directory: $pluginsDir")
        }

        val jars = listPluginJars()

        for (jar in jars) {
            val yamlText = readPluginYaml(jar) ?: continue
            val desc = parseDescriptor(yamlText, jar.fileName.toString()) ?: continue

            val name = desc.name

            if (_pluginJarMap.putIfAbsent(name, jar) != null) {
                logger.error("Duplicate plugin name '$name'. Jar $jar is ignored.")
                continue
            }
            logger.info("Discovered plugin: $name v${desc.version}")
        }
    }

    /**
     * Lists all jars in the plugins directory.
     */
    private fun listPluginJars(): List<Path> =
        Files.newDirectoryStream(pluginsDir) { it.toString().endsWith(".jar", ignoreCase = true) }
            .use { ds -> ds.toList() }

    fun readPluginYaml(jar: Path): String? {
        JarFile(jar.toFile()).use { jf ->
            val entry = jf.getJarEntry("plugin.yml")
            if (entry == null) {
                logger.error("Jar $jar is missing plugin.yml, skipping.")
                return null
            }
            jf.getInputStream(entry).use { ins ->
                return ins.reader(Charsets.UTF_8).readText()
            }
        }
    }

    fun parseDescriptor(yamlText: String, jarName: String): PluginDescriptor? {
        return try {
            val yaml = Yaml()
            val desc = yaml.decodeFromString(PluginDescriptor.serializer(), yamlText)
            desc
        } catch (e: Exception) {
            logger.error("Failed to parse plugin.yml in $jarName", e)
            null
        }
    }
}