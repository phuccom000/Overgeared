package net.stirdrem.overgeared.platform;

import net.stirdrem.overgeared.OvergearedMod;

import java.util.ServiceLoader;

/**
 * Loads the loader-specific implementations of the platform services.
 * Each loader project provides its implementation via {@code META-INF/services}.
 */
public final class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        OvergearedMod.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
