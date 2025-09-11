package io.aitchn.dcnucleus.console;

import io.aitchn.dcnucleus.DCNucleus;
import io.aitchn.dcnucleus.api.console.ConsoleCommand;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConsoleCommandManager {
    public static final ConsoleCommandManager INSTANCE = new ConsoleCommandManager();
    private static final Logger logger = DCNucleus.logger;
    private static final Map<String, ConsoleCommand> commands = new ConcurrentHashMap<>();

    private ConsoleCommandManager() {}

    public void register(ConsoleCommand command) {
        commands.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            commands.put(alias.toLowerCase(), command);
        }
        logger.info("Registered command: {}", command.getName());
    }

    public void unregister(ConsoleCommand command) {
        commands.remove(command.getName().toLowerCase());
        for (String alias : command.getAliases()) {
            commands.remove(alias.toLowerCase());
        }
        logger.info("Unregistered command: {}", command.getName());
    }

    public boolean executeCommand(String input) {
        String[] parts = input.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) {
            return false;
        }

        String commandName = parts[0].toLowerCase();
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        ConsoleCommand command = commands.get(commandName);
        if (command == null) {
            logger.warn("Unknown command: {}", commandName);
            return false;
        }

        try {
            return command.execute(args);
        } catch (Exception e) {
            logger.error("Error executing command '{}'", commandName, e);
            return false;
        }
    }

    public List<String> getTabCompletions(String input) {
        String[] parts = input.split("\\s+");

        // 補全指令名稱
        if (parts.length == 1) {
            String partial = parts[0].toLowerCase();
            return commands.keySet().stream()
                    .filter(k -> k.startsWith(partial))
                    .sorted()
                    .toList();
        }

        // 補全參數
        String commandName = parts[0].toLowerCase();
        ConsoleCommand command = commands.get(commandName);
        if (command == null) return Collections.emptyList();

        String[] args = Arrays.copyOfRange(parts, 1, parts.length);
        return command.tabComplete(args);
    }

    public Collection<ConsoleCommand> getAllCommands() {
        return commands.values().stream()
                .distinct()
                .toList();
    }
}
