package io.aitchn.dcnucleus.server.watcher;

import io.aitchn.dcnucleus.DCNucleus;
import io.aitchn.dcnucleus.console.ConsoleCommandManager;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsoleCommandInputHandler implements AutoCloseable{
    private static final Logger logger = DCNucleus.logger;
    private final ConsoleCommandManager commandManager = ConsoleCommandManager.INSTANCE;
    private final Terminal terminal;
    private final LineReader lineReader;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ConsoleCommandInput");
        t.setDaemon(false);
        return t;
    });

    public ConsoleCommandInputHandler() {
        Terminal term;
        try {
            term = TerminalBuilder.builder()
                    .system(true)
                    .encoding("UTF-8")
                    .build();
        } catch (Exception e) {
            logger.warn("Failed to create system terminal, falling back to dumb terminal");
            try {
                term = TerminalBuilder.builder()
                        .dumb(true)
                        .build();
            } catch (IOException ex) {
                throw new RuntimeException("Failed to initialize terminal", ex);
            }
        }
        this.terminal = term;

        this.lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer((reader, line, candidates) -> {
                    var completions = commandManager.getTabCompletions(line.line());
                    completions.forEach(c -> candidates.add(new Candidate(c)));
                })
                .build();
    }

    public void startInputLoop() {
        executor.execute(() -> {
            logger.info("Command input ready. Type 'help' for available commands.");

            while (true) {
                try {
                    String input = lineReader.readLine("> ");
                    if (input != null && !input.isBlank()) {
                        commandManager.executeCommand(input);
                    }
                } catch (UserInterruptException e) {
                    // Ctrl+C
                    logger.info("Use 'stop' command to shutdown properly");
                } catch (EndOfFileException e) {
                    // Ctrl+D
                    break;
                } catch (Exception e) {
                    logger.error("Error reading input", e);
                }
            }
        });
    }

    @Override
    public void close() {
        executor.shutdownNow();
        try {
            terminal.close();
        } catch (IOException e) {
            logger.error("Failed to close terminal", e);
        }
    }
}
