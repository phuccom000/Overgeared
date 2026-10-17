package net.stirdrem.overgeared.platform;

import net.fabricmc.loader.api.FabricLoader;
import net.stirdrem.overgeared.config.FabricServerConfig;
import net.stirdrem.overgeared.config.ServerConfigCommon;
import net.stirdrem.overgeared.platform.services.IPlatformHelper;

public class FabricPlatformHelper implements IPlatformHelper {
    private final ServerConfigCommon config;

    public FabricPlatformHelper() {
        this.config = new FabricServerConfig();
    }

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public ServerConfigCommon getConfig() {
        return config;
    }
}
