package io.aitchn.dcnucleus.server

import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchEvent
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.io.path.isDirectory

class PluginDirWatcher(
    private val pluginsDir: Path,
    private val onStableChange: () -> Unit,
    private val debounceMs: Long = 500
) : AutoCloseable {

    private val logger = LoggerFactory.getLogger("[PluginDir]")
    private val watchService = FileSystems.getDefault().newWatchService()

    private val watchExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "PluginDir-Watch").apply {
            isDaemon = false
        }
    }
    private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "PluginDirWatcher-Scheduler").apply {
            isDaemon = false
        }
    }

    @Volatile private var closed = false

    fun start() {
        if (!pluginsDir.isDirectory()) {
            throw IllegalArgumentException("plugins dir not found: $pluginsDir")
        }

        pluginsDir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
        logger.info("Plugin directory watcher started")

        watchExecutor.execute {
            var scheduled = false
            var lastFuture = null as java.util.concurrent.ScheduledFuture<*>?

            while (!closed) {
                val key = try {
                    watchService.poll(500, TimeUnit.MILLISECONDS) ?: continue
                } catch (_: InterruptedException) {
                    break
                } catch (e: Exception) {
                    logger.error("Error in watch service", e)
                    break
                }

                var changed = false
                val events = key.pollEvents()

                for (event in events) {
                    val kind = event.kind()
                    if (kind == OVERFLOW) {
                        logger.warn("Watch service overflow - some events may have been lost")
                        continue
                    }

                    @Suppress("UNCHECKED_CAST")
                    val ev = event as WatchEvent<Path>
                    val filename = ev.context()

                    // 關心 .jar
                    if (!filename.toString().endsWith(".jar", ignoreCase = true)) continue

                    logger.info("Plugin file changed: $filename")
                    changed = true
                }

                key.reset()

                if (changed) {
                    // 取消先前的排程
                    if (scheduled) {
                        lastFuture?.cancel(false)
                    }

                    // 排程新的重整任務
                    lastFuture = scheduledExecutor.schedule({
                        try {
                            onStableChange()
                        } catch (e: Exception) {
                            logger.error("Plugin refresh failed", e)
                        } finally {
                            scheduled = false
                        }
                    }, debounceMs, TimeUnit.MILLISECONDS)
                    scheduled = true
                }
            }
        }
    }

    override fun close() {
        closed = true
        try { watchService.close() } catch (_: Exception) {}
        watchExecutor.shutdownNow()
        scheduledExecutor.shutdownNow()
        logger.info("Plugin directory watcher stopped")
    }
}
