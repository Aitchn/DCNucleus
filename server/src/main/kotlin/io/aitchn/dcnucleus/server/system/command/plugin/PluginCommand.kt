package io.aitchn.dcnucleus.server.system.command.plugin

import io.aitchn.dcnucleus.api.system.SystemCommand
import io.aitchn.dcnucleus.server.plugin.PluginManager
import org.slf4j.LoggerFactory

class PluginCommand: SystemCommand {
    override val name: String = "plugin"
    override val description: String = "Manage plugins"
    override val usage: String = "plugin <list|enable|disable|reload|info> [name]"

    private val logger = LoggerFactory.getLogger("[Plugin]")


    override fun execute(args: Array<String>): Boolean {
        if (args.isEmpty()) {
            logger.info("Usage: $usage")
            showAvailableActions()
            return false
        }

        return when (args[0].lowercase()) {
            "list", "l" -> executeList(args)
            "enable", "e" -> executeEnable(args)
            "disable", "d" -> executeDisable(args)
            "reload", "r" -> executeReload(args)
            "info", "i" -> executeInfo(args)
            else -> {
                logger.warn("Unknown plugin action: ${args[0]}")
                showAvailableActions()
                false
            }
        }
    }

    private fun executeList(args: Array<String>): Boolean {
        val loaded = PluginManager.loadedPlugins()
        val available = PluginManager.availablePlugins()
        val all = loaded + available

        if (all.isEmpty()) {
            logger.info("No plugins found")
            return true
        }

        logger.info("Plugin Status:")
        logger.info("=".repeat(40))

        // 顯示已載入插件
        if (loaded.isNotEmpty()) {
            logger.info("Loaded (${loaded.size}):")
            loaded.sorted().forEach { name ->
                logger.info("  - $name")
            }
        }

        // 顯示可用但未載入插件
        if (available.isNotEmpty()) {
            logger.info("Available (${available.size}):")
            available.sorted().forEach { name ->
                logger.info("  - $name")
            }
        }

        return true
    }

    private fun executeEnable(args: Array<String>): Boolean {
        if (args.size < 2) {
            logger.warn("Usage: plugin enable <name>")
            return false
        }

        val pluginName = args[1]
        val success = PluginManager.enable(pluginName)

        if (success) {
            logger.info("Plugin '$pluginName' enabled successfully")
        } else {
            logger.error("Failed to enable plugin '$pluginName'")
        }

        return success
    }

    private fun executeDisable(args: Array<String>): Boolean {
        if (args.size < 2) {
            logger.warn("Usage: plugin disable <name>")
            return false
        }

        val pluginName = args[1]
        val success = PluginManager.disable(pluginName)

        if (success) {
            logger.info("Plugin '$pluginName' disabled successfully")
        } else {
            logger.error("Failed to disable plugin '$pluginName'")
        }

        return success
    }

    private fun executeReload(args: Array<String>): Boolean {
        if (args.size < 2) {
            logger.info("Reloading all plugins...")
            val (added, removed, reloaded) = PluginManager.refresh()
            logger.info("Reload result: +$added / -$removed / reload=$reloaded")
            return true
        } else {
            val pluginName = args[1]
            logger.info("Reloading plugin '$pluginName'...")

            val wasLoaded = PluginManager.loadedPlugins().contains(pluginName)
            if (!wasLoaded) {
                logger.warn("Plugin '$pluginName' is not currently loaded")
                return false
            }

            val disableSuccess = PluginManager.disable(pluginName)
            if (!disableSuccess) {
                logger.error("Failed to disable plugin '$pluginName' for reload")
                return false
            }

            // 釋放資源
            Thread.sleep(100)

            val enableSuccess = PluginManager.enable(pluginName)
            if (enableSuccess) {
                logger.info("Plugin '$pluginName' reloaded successfully")
            } else {
                logger.error("Failed to re-enable plugin '$pluginName'")
            }

            return enableSuccess
        }
    }

    private fun executeInfo(args: Array<String>): Boolean {
        if (args.size < 2) {
            logger.warn("Usage: plugin info <name>")
            return false
        }

        val pluginName = args[1]
        val isLoaded = PluginManager.loadedPlugins().contains(pluginName)
        val isAvailable = PluginManager.availablePlugins().contains(pluginName)

        if (!isLoaded && !isAvailable) {
            logger.warn("Plugin '$pluginName' not found")
            return false
        }

        logger.info("Plugin Information: $pluginName")
        logger.info("=".repeat(30))
        logger.info("Status: ${if (isLoaded) "Loaded" else "Available"}")

        // 可擴展訊息

        return true
    }

    private fun showAvailableActions() {
        logger.info("Available actions:")
        logger.info("  list (l)    - Show all plugins")
        logger.info("  enable (e)  - Enable a plugin")
        logger.info("  disable (d) - Disable a plugin")
        logger.info("  reload (r)  - Reload plugin(s)")
        logger.info("  info (i)    - Show plugin information")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.size <= 1) {
            val partial = args.getOrNull(0)?.lowercase() ?: ""
            return listOf("list", "enable", "disable", "reload", "info")
                .filter { it.startsWith(partial) }
        }

        if (args.size == 2) {
            val action = args[0].lowercase()
            val partial = args[1].lowercase()

            return when (action) {
                "enable", "e" -> PluginManager.availablePlugins()
                    .filter { it.lowercase().startsWith(partial) }
                    .sorted()
                "disable", "d", "reload", "r", "info", "i" -> PluginManager.loadedPlugins()
                    .filter { it.lowercase().startsWith(partial) }
                    .sorted()
                else -> emptyList()
            }
        }

        return emptyList()
    }

}