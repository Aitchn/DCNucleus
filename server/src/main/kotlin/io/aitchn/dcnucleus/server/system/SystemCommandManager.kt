package io.aitchn.dcnucleus.server.system

import io.aitchn.dcnucleus.api.system.SystemCommand
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

object SystemCommandManager {
    private val logger: Logger = LoggerFactory.getLogger("[SystemCommand]")
    private val commands = ConcurrentHashMap<String, SystemCommand>()

    fun register(command: SystemCommand) {
        commands[command.name.lowercase()] = command
        command.aliases.forEach { alias ->
            commands[alias.lowercase()] = command
        }
        logger.info("Registered command: ${command.name}")
    }

    fun executeCommand(input: String): Boolean {
        val parts = input.trim().split(" ")
        if (parts.isEmpty() || parts[0].isBlank()) return false

        val commandName = parts[0].lowercase()
        val args = parts.drop(1).toTypedArray()

        val command = commands[commandName]
        if (command == null) {
            logger.warn("Unknown command: $commandName")
            return false
        }

        return try {
            command.execute(args)
        } catch (e: Exception) {
            logger.error("Error executing command '$commandName'", e)
            false
        }
    }

    fun getTabCompletions(input: String): List<String> {
        val parts = input.split(" ")

        // 補全指令名稱
        if (parts.size == 1) {
            val partial = parts[0].lowercase()
            return commands.keys
                .filter { it.startsWith(partial) }
                .sorted()
        }

        // 補全指令參數
        val commandName = parts[0].lowercase()
        val command = commands[commandName] ?: return emptyList()
        val args = parts.drop(1).toTypedArray()

        return command.tabComplete(args)
    }

    fun getAllCommands(): Collection<SystemCommand> = commands.values.distinctBy { it.name }
}
