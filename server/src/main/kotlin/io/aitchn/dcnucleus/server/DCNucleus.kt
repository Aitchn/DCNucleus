package io.aitchn.dcnucleus.server

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Paths


private val logger: Logger = LoggerFactory.getLogger("[DCNucleus-Server]")

fun main() {
    val path = Paths.get("").toAbsolutePath().normalize()
    val pluginLoader = PluginLoader(path.resolve("plugins"))

    logger.info("DC Nucleus is running. path=$path")
}