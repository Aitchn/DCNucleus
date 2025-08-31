package io.aitchn.dcnucleus.server

import io.aitchn.dcnucleus.server.system.SystemCommandInputHandler
import io.aitchn.dcnucleus.server.system.SystemCommandManager
import io.aitchn.dcnucleus.server.system.command.plugin.PluginCommand
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Paths


private val logger: Logger = LoggerFactory.getLogger("[DCNucleus-Server]")

fun main() {
    val path = Paths.get("").toAbsolutePath().normalize()
    val pluginLoader = PluginLoader(path.resolve("plugins"))
    val pluginManager = PluginManager
    pluginManager.configure(pluginLoader)

    logger.info("Loading existing plugins...")
    pluginManager.enableAll()

    val watcher = PluginDirWatcher(
        path.resolve("plugins"),
        onStableChange = {
            val (added, removed, reloaded) = PluginManager.refresh()
            logger.info("Plugin refresh: +$added / -$removed / reload=$reloaded")
        }
    )

    watcher.start()

    SystemCommandManager.register(PluginCommand())

    val commandHandler = SystemCommandInputHandler()
    commandHandler.startInputLoop()


    Runtime.getRuntime().addShutdownHook(Thread {
        try { watcher.close() } catch (_: Exception) {}
        try { commandHandler.close() } catch (_: Exception) {}
        PluginManager.disableAll()
        logger.info("DC Nucleus shutdown complete")
    })

    logger.info("DC Nucleus is running. path= $path")
    while (true) {
        Thread.sleep(1000L)
    }
}