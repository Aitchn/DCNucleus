package io.aitchn.dcnucleus.api;

import io.aitchn.dcnucleus.api.console.ConsoleCommand;

public interface ServerManager {

    void registerConsole(ConsoleCommand command, Plugin plugin);
    void unregisterConsole(ConsoleCommand command, Plugin plugin);
}
