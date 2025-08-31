package io.aitchn.dcnucleus.server

import io.aitchn.dcnucleus.server.system.SystemCommandInputHandler
import io.aitchn.dcnucleus.server.system.SystemCommandManager
import io.aitchn.dcnucleus.server.system.command.plugin.PluginCommand
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Paths


class DCNucleus {
    private val logger: Logger = LoggerFactory.getLogger("[DCNucleus]")

    private lateinit var pluginLoader: PluginLoader
    private lateinit var pluginWatcher: PluginDirWatcher
    private lateinit var commandHandler: SystemCommandInputHandler

    fun start() {
        logger.info("Starting DC Nucleus...")

        initializeCore()
        initializePluginSystem()
        initializeCommandSystem()
        registerShutdownHook()

        logger.info("DC Nucleus is running")
        keepAlive()
    }

    /**
     * 初始化核心
     */
    private fun initializeCore() {
        val pluginsPath = getCurrentPath().resolve("plugins")

        // 初始化插件載入器
        pluginLoader = PluginLoader(pluginsPath)
        PluginManager.configure(pluginLoader)

        logger.info("Plugin loader initialized")
    }

    /**
     * 插件相關
     */
    private fun initializePluginSystem() {
        logger.info("Loading existing plugins...")
        val loadedCount = PluginManager.enableAll()
        logger.info("Loaded $loadedCount plugin(s)")

        // 啟動插件目錄監控
        pluginWatcher = PluginDirWatcher(
            getCurrentPath().resolve("plugins"),
            onStableChange = ::handlePluginDirectoryChange
        )
        pluginWatcher.start()

        logger.info("Plugin system initialized")
    }

    /**
     * 註冊控制台指令
     */
    private fun initializeCommandSystem() {
        registerSystemCommands()

        // 指令輸入處理器
        commandHandler = SystemCommandInputHandler()
        commandHandler.startInputLoop()

        logger.info("Command system initialized")
    }

    /**
     * 註冊所有指令
     */
    private fun registerSystemCommands() {
        SystemCommandManager.register(PluginCommand())

        logger.info("System commands registered")
    }

    /**
     * 處理 Plugins 更改
     */
    private fun handlePluginDirectoryChange() {
        val (added, removed, reloaded) = PluginManager.refresh()
        if (added > 0 || removed > 0 || reloaded > 0) {
            logger.info("Plugin refresh: +$added / -$removed / reload=$reloaded")
        }
    }

    /**
     *
     */
    private fun registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(Thread {
            shutdown()
        })
    }

    private fun shutdown() {
        logger.info("Shutting down DC Nucleus...")

        // 插件監控器
        try {
            pluginWatcher.close()
            logger.info("Plugin watcher stopped")
        } catch (e: Exception) {
            logger.warn("Error closing plugin watcher", e)
        }

        // 指令處理器
        try {
            commandHandler.close()
            logger.info("Command handler stopped")
        } catch (e: Exception) {
            logger.warn("Error closing command handler", e)
        }

        // 停用插件
        val disabledCount = PluginManager.disableAll()
        logger.info("Disabled $disabledCount plugin(s)")

        logger.info("DC Nucleus shutdown complete")
    }

    private fun getCurrentPath() = Paths.get("").toAbsolutePath().normalize()

    private fun keepAlive() {
        while (true) {
            Thread.sleep(1000L)
        }
    }
}

fun main() {
    val dcNucleus = DCNucleus()
    dcNucleus.start()
}
