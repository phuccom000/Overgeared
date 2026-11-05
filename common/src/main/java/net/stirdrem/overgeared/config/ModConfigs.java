package net.stirdrem.overgeared.config;

import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.core.ConfigWatcher;
import net.stirdrem.overgeared.platform.Services;

import java.nio.file.Path;

/**
 * Loads the mod's config files. Same file names and layout as the NeoForge config system used before,
 * so existing configs carry over.
 */
public final class ModConfigs {
    public static final String COMMON_FILE = OvergearedMod.MOD_ID + "-common.toml";
    public static final String CLIENT_FILE = OvergearedMod.MOD_ID + "-client.toml";

    private ModConfigs() {
    }

    public static void load() {
        Path configDir = Services.PLATFORM.getConfigDir();
        ServerConfig.SERVER_CONFIG.load(configDir.resolve(COMMON_FILE));
        AnvilTierConfig.load(configDir);
        ConfigWatcher.watch(ServerConfig.SERVER_CONFIG);
        if (Services.PLATFORM.isPhysicalClient()) {
            ClientConfig.CLIENT_CONFIG.load(configDir.resolve(CLIENT_FILE));
            ConfigWatcher.watch(ClientConfig.CLIENT_CONFIG);
        }
    }
}
