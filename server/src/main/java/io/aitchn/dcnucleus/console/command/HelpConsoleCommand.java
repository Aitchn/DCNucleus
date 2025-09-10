package io.aitchn.dcnucleus.console.command;

import io.aitchn.dcnucleus.DCNucleus;
import io.aitchn.dcnucleus.api.console.ConsoleCommand;
import io.aitchn.dcnucleus.console.ConsoleCommandManager;

public class HelpConsoleCommand implements ConsoleCommand {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "List all available commands.";
    }

    @Override
    public String getUsage() {
        return "help";
    }

    @Override
    public boolean execute(String[] args) {
        DCNucleus.logger.info("Available commands:");
        for (ConsoleCommand cmd : ConsoleCommandManager.getAllCommands()) {
            DCNucleus.logger.info("- {} : {} (Usage: {})",
                    cmd.getName(), cmd.getDescription(), cmd.getUsage());
        }
        return true;
    }
}
