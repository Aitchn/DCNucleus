package io.aitchn.dcnucleus;

import io.aitchn.dcnucleus.console.ConsoleCommandManager;
import io.aitchn.dcnucleus.console.command.HelpConsoleCommand;
import io.aitchn.dcnucleus.console.command.PluginConsoleCommand;
import io.aitchn.dcnucleus.console.command.StopConsoleCommand;
import io.aitchn.dcnucleus.plugin.PluginManager;
import io.aitchn.dcnucleus.server.watcher.ConsoleCommandInputHandler;
import io.aitchn.dcnucleus.server.watcher.PluginDirWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class DCNucleus {
    public static final Logger logger = LoggerFactory.getLogger("[DCNucleus]");

    public static void main(String[] args) {
        logger.info("DCNucleus 正在啟動");

        // === === Plugin System === ===
        PluginManager pluginManager = PluginManager.getInstance();
        pluginManager.enableAll();

        Path pluginDir = Path.of("plugins");
        PluginDirWatcher watcher = new PluginDirWatcher(pluginDir, pluginManager::refresh);

        Thread watcherThread = new Thread(watcher, "PluginDirWatcher");
        watcherThread.setDaemon(true);
        watcherThread.start();

        // === === Console System === ===
        ConsoleCommandManager.register(new PluginConsoleCommand());
        ConsoleCommandManager.register(new StopConsoleCommand());
        ConsoleCommandManager.register(new HelpConsoleCommand());

        try (ConsoleCommandInputHandler inputHandler = new ConsoleCommandInputHandler()) {
            inputHandler.startInputLoop();
        } catch (Exception e) {
            logger.error("Failed to start console input handler", e);
        }

        logger.info("啟動");
    }
}
