package io.aitchn.dcnucleus.server

import io.aitchn.dcnucleus.api.DCNucleusPlugin
import java.nio.file.Path

data class LoadedPlugin(
    val name: String,
    val jar: Path,
    val classLoader: PluginClassLoader,
    val instance: DCNucleusPlugin
)