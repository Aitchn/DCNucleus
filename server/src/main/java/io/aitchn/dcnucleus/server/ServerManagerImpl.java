package io.aitchn.dcnucleus.server;

import io.aitchn.dcnucleus.api.Plugin;
import io.aitchn.dcnucleus.api.ServerManager;
import io.aitchn.dcnucleus.api.console.ConsoleCommand;
import io.aitchn.dcnucleus.console.ConsoleCommandManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerManagerImpl implements ServerManager {
    private final ConsoleCommandManager commandManager = ConsoleCommandManager.INSTANCE;
    private final Map<Plugin, List<ConsoleCommand>> pluginConsoleCommands = new HashMap<>();

    @Override
    public void registerConsole(ConsoleCommand command, Plugin plugin) {
        commandManager.register(command);
        pluginConsoleCommands.computeIfAbsent(plugin, p -> new ArrayList<>()).add(command);
    }

    @Override
    public void unregisterConsole(ConsoleCommand command, Plugin plugin) {
        List<ConsoleCommand> cmds = pluginConsoleCommands.remove(plugin);
        if (cmds != null) {
            cmds.forEach(commandManager::unregister);
        }
    }
}
