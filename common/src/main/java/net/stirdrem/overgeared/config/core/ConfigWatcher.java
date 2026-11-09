package net.stirdrem.overgeared.config.core;

import net.stirdrem.overgeared.OvergearedMod;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Reloads config files when they are edited on disk while the game is running, as NeoForge's config system did.
 */
public final class ConfigWatcher {
    /** Editors often write a file in several steps; wait for it to settle before reading. */
    private static final long DEBOUNCE_MS = 300;

    private static final Map<Path, ConfigSpec> WATCHED = new ConcurrentHashMap<>();
    private static final Set<Path> WATCHED_DIRS = ConcurrentHashMap.newKeySet();
    private static WatchService service;

    private ConfigWatcher() {
    }

    /** Watches the file {@code spec} was loaded from. Call after {@link ConfigSpec#load(Path)}. */
    public static synchronized void watch(ConfigSpec spec) {
        Path file = spec.getFile();
        if (file == null) return;
        file = file.toAbsolutePath().normalize();
        Path dir = file.getParent();

        try {
            if (service == null) {
                service = FileSystems.getDefault().newWatchService();
                Thread thread = new Thread(ConfigWatcher::run, "Overgeared Config Watcher");
                thread.setDaemon(true);
                thread.start();
            }
            if (WATCHED_DIRS.add(dir)) {
                dir.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            }
            WATCHED.put(file, spec);
        } catch (IOException | UnsupportedOperationException e) {
            OvergearedMod.LOGGER.warn("Can't watch config {} for changes; edits will apply after a restart", file, e);
        }
    }

    private static void run() {
        Map<Path, Long> due = new HashMap<>();
        while (true) {
            try {
                WatchKey key = due.isEmpty()
                        ? service.take()
                        : service.poll(DEBOUNCE_MS, TimeUnit.MILLISECONDS);

                if (key != null) {
                    Path dir = (Path) key.watchable();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context() instanceof Path name) {
                            Path file = dir.resolve(name).toAbsolutePath().normalize();
                            if (WATCHED.containsKey(file)) {
                                due.put(file, System.currentTimeMillis() + DEBOUNCE_MS);
                            }
                        }
                    }
                    key.reset();
                }

                long now = System.currentTimeMillis();
                Set<Path> ready = new HashSet<>();
                for (Iterator<Map.Entry<Path, Long>> it = due.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<Path, Long> entry = it.next();
                    if (entry.getValue() <= now) {
                        ready.add(entry.getKey());
                        it.remove();
                    }
                }
                for (Path file : ready) {
                    try {
                        WATCHED.get(file).reload();
                    } catch (RuntimeException e) {
                        OvergearedMod.LOGGER.error("Failed to reload config {}", file, e);
                    }
                }
            } catch (InterruptedException | ClosedWatchServiceException e) {
                return;
            }
        }
    }
}
