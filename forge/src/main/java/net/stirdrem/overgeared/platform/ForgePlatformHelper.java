package net.stirdrem.overgeared.platform;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.stirdrem.overgeared.config.ForgeServerConfig;
import net.stirdrem.overgeared.config.ServerConfigCommon;
import net.stirdrem.overgeared.platform.services.IPlatformHelper;

public class ForgePlatformHelper implements IPlatformHelper {
    private final ServerConfigCommon config;

    public ForgePlatformHelper() {
        this.config = new ForgeServerConfig();
    }

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public ServerConfigCommon getConfig() {
        return config;
    }
}