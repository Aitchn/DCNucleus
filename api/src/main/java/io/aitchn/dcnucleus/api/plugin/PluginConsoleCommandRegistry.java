package io.aitchn.dcnucleus.api.plugin;

import io.aitchn.dcnucleus.api.Plugin;
import io.aitchn.dcnucleus.api.console.ConsoleCommand;

public interface PluginConsoleCommandRegistry {

    void registerConsoleCommand(ConsoleCommand command, Plugin plugin);

    void unregisterConsoleCommand(ConsoleCommand command, Plugin plugin);
}
