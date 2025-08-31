package io.aitchn.dcnucleus.server.plugin

import io.aitchn.dcnucleus.api.DCNucleusPlugin
import io.aitchn.dcnucleus.server.plugin.PluginClassLoader
import java.nio.file.Path

data class LoadedPlugin(
    val name: String,
    val jar: Path,
    val classLoader: PluginClassLoader,
    val instance: DCNucleusPlugin
)