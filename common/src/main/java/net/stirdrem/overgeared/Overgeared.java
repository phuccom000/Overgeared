package net.stirdrem.overgeared;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.stirdrem.overgeared.config.ServerConfigCommon;
import net.stirdrem.overgeared.platform.Services;
import org.slf4j.Logger;

public class Overgeared {
    public static final String MOD_ID = Constants.MOD_ID;
    public static final String MOD_NAME = Constants.MOD_NAME;
    public static final Logger LOGGER = Constants.LOG;

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Overgeared.MOD_ID, path);
    }

    public static void init() {

        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
        Constants.LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));
        if (Services.PLATFORM.isModLoaded(Constants.MOD_ID)) {

            Constants.LOG.info("Hello to Overgeared");
        }
    }

    public static ServerConfigCommon getConfig() {
        return Services.PLATFORM.getConfig();
    }
}