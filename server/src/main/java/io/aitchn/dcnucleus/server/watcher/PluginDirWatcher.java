package io.aitchn.dcnucleus.server.watcher;

import io.aitchn.dcnucleus.DCNucleus;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

public class PluginDirWatcher implements Runnable {
    private final Path pluginDir;
    private final Runnable callback;
    private final Logger logger = DCNucleus.logger;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final long debounceMillis = 1000L; // 防止多次觸發
    private ScheduledFuture<?> debounceFuture;

    public PluginDirWatcher(Path pluginDir, Runnable callback) {
        this.pluginDir = pluginDir;
        this.callback = callback;
    }

    @Override
    public void run() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            pluginDir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            logger.info("Watching plugin directory: {}", pluginDir);

            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path changed = (Path) event.context();

                    if (changed.toString().endsWith(".jar")) {
                        logger.debug("Plugin dir change detected: {} {}", kind.name(), changed);
                        debounceRefresh();
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.error("PluginDirWatcher stopped", e);
            Thread.currentThread().interrupt();
        }
    }

    private void debounceRefresh() {
        if (debounceFuture != null && !debounceFuture.isDone()) {
            debounceFuture.cancel(false);
        }
        debounceFuture = scheduler.schedule(callback, debounceMillis, TimeUnit.MILLISECONDS);
    }
}
