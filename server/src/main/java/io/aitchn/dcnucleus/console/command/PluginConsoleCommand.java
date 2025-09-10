package io.aitchn.dcnucleus.console.command;

import io.aitchn.dcnucleus.DCNucleus;
import io.aitchn.dcnucleus.api.console.ConsoleCommand;
import io.aitchn.dcnucleus.plugin.LoadedPlugin;
import io.aitchn.dcnucleus.plugin.PluginDescriptor;
import io.aitchn.dcnucleus.plugin.PluginManager;
import org.slf4j.Logger;

import java.util.Map;

public class PluginConsoleCommand implements ConsoleCommand {
    private final PluginManager pluginManager = PluginManager.getInstance();
    private final Logger logger = DCNucleus.logger;

    @Override
    public String getName() {
        return "plugin";
    }

    @Override
    public String getDescription() {
        return "Manage plugins (list, enable, disable, reload, info)";
    }

    @Override
    public String getUsage() {
        return "plugin <list|enable|disable|reload|info> [name]";
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length == 0) {
            logger.info("Usage: {}", getUsage());
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> list();
            case "enable" -> {
                if (args.length < 2) {
                    logger.warn("Usage: plugin enable <name>");
                } else {
                    pluginManager.enable(args[1]);
                }
            }
            case "disable" -> {
                if (args.length < 2) {
                    logger.warn("Usage: plugin disable <name>");
                } else {
                    pluginManager.disable(args[1]);
                }
            }
            case "reload" -> {
                if (args.length < 2) {
                    logger.warn("Usage: plugin reload <name|all>");
                } else if (args[1].equalsIgnoreCase("all")) {
                    pluginManager.disableAll();
                    pluginManager.enableAll();
                } else {
                    pluginManager.disable(args[1]);
                    pluginManager.enable(args[1]);
                }
            }
            case "info" -> {
                if (args.length < 2) {
                    logger.warn("Usage: plugin info <name>");
                } else {
                    info(args[1]);
                }
            }
            default -> logger.warn("Unknown command: {}", args[0]);
        }
        return true;
    }

    private void list() {
        Map<String, LoadedPlugin> loaded = pluginManager.getLoadedPlugins();
        if (loaded.isEmpty()) {
            logger.info("No plugins loaded.");
            return;
        }

        logger.info("Loaded plugins:");
        loaded.values().forEach(plugin -> {
            PluginDescriptor desc = plugin.getDescriptor();
            logger.info("- {} v{}", desc.getName(), desc.getVersion());
        });
    }

    private void info(String name) {
        LoadedPlugin plugin = pluginManager.getLoadedPlugins().get(name);
        if (plugin == null) {
            logger.warn("Plugin {} is not loaded", name);
            return;
        }
        PluginDescriptor desc = plugin.getDescriptor();
        logger.info("Plugin Info:");
        logger.info("Name: {}", desc.getName());
        logger.info("Version: {}", desc.getVersion());
        logger.info("Entrypoint: {}", desc.getEntrypoint());
    }
}
