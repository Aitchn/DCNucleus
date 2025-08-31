package io.aitchn.dcnucleus.server

import java.nio.file.Files
import java.nio.file.Path

class PluginLoader(
    private val pluginsDir: Path
) {

    init {
        if (Files.notExists(pluginsDir)) {
            Files.createDirectories(pluginsDir)
        }
    }
}