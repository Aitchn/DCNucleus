package io.aitchn.dcnucleus.server.system

import org.jline.reader.Candidate
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

class SystemCommandInputHandler : AutoCloseable {
    private val logger = LoggerFactory.getLogger("[SystemCommandInput]")
    private val terminal: Terminal
    private val lineReader: LineReader
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "SystemCommandInput").apply { isDaemon = false }
    }

    init {
        terminal = try {
            TerminalBuilder.builder()
                .system(true)
                .encoding("UTF-8")
                .build()
        } catch (_: Exception) {
            logger.warn("Failed to create system terminal, falling back to dumb terminal")
            TerminalBuilder.builder()
                .dumb(true)
                .build()
        }

        lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .completer { _, line, candidates ->
                val completions = SystemCommandManager.getTabCompletions(line.line())
                candidates.addAll(completions.map { Candidate(it) })
            }
            .build()
    }


    fun startInputLoop() {
        executor.execute {
            logger.info("Command input ready. Type 'help' for available commands.")

            while (true) {
                try {
                    val input = lineReader.readLine("> ")
                    if (input.isNotBlank()) {
                        SystemCommandManager.executeCommand(input)
                    }
                } catch (_: UserInterruptException) {
                    // Ctrl+C
                    logger.info("Use 'stop' command to shutdown properly")
                } catch (_: EndOfFileException) {
                    // Ctrl+D
                    break
                } catch (e: Exception) {
                    logger.error("Error reading input", e)
                }
            }
        }
    }

    override fun close() {
        executor.shutdownNow()
        terminal.close()
    }



}