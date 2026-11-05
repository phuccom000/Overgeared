package net.stirdrem.overgeared;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.DispenserBlock;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.UpgradeArrowDispenseBehavior;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.command.ModCommands;
import net.stirdrem.overgeared.compat.accessories.AttributeModifierHandler;
import net.stirdrem.overgeared.config.ClientConfig;
import net.stirdrem.overgeared.config.AnvilTierConfig;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.event.ReloadListenerRegistry;
import net.stirdrem.overgeared.item.ModCreativeModeTabs;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.loot.ModLootModifiers;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.ModRecipes;
import net.stirdrem.overgeared.screen.ModMenuTypes;
import net.stirdrem.overgeared.sound.AnvilSoundServices;
import net.stirdrem.overgeared.sound.FabricAnvilSoundService;
import net.stirdrem.overgeared.sound.ModSounds;
import org.jetbrains.annotations.Nullable;


public class OvergearedFabric implements ModInitializer {

    @Nullable
    private static MinecraftServer server;

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Overgeared.MOD_ID, path);
    }

    /**
     * Fabric equivalent of Forge's ServerLifecycleHooks.getCurrentServer() - used sparingly,
     * only where a recipe/handler doesn't already have a World reference to work from.
     */
    @Nullable
    public static MinecraftServer getServer() {
        return server;
    }

    @Override
    public void onInitialize() {
        Overgeared.LOGGER.info("Overgeared: initializing Fabric port");

        // Before the main config loads: the default tiers take the old enableTierA/enableTierB options from it
        AnvilTierConfig.createDefaultFileIfMissing();

        // Must run before any other registration: several classes (ModToolTiers,
        // item constructors) read config values in static initializers,
        // which run the first time those classes are touched.
        ServerConfig.loadConfig(FabricLoader.getInstance().getConfigDir().resolve("overgeared-server.json"));
        ClientConfig.loadConfig(ClientConfig.CLIENT_CONFIG, FabricLoader.getInstance().getConfigDir().resolve("overgeared-client.json"));

        ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);

        // Force static init / registration for each registry class.
        ModItems.init();
        ModBlocks.init();
        ModBlocks.registerAliases();
        ModBlocks.registerTieredAnvils();
        ModRecipes.init();
        ModRecipeTypes.init();

        UpgradeArrowDispenseBehavior dispenseBehavior = new UpgradeArrowDispenseBehavior();
        DispenserBlock.registerBehavior(ModItems.LINGERING_ARROW.get(), dispenseBehavior);
        DispenserBlock.registerBehavior(ModItems.IRON_UPGRADE_ARROW.get(), dispenseBehavior);
        DispenserBlock.registerBehavior(ModItems.STEEL_UPGRADE_ARROW.get(), dispenseBehavior);
        DispenserBlock.registerBehavior(ModItems.DIAMOND_UPGRADE_ARROW.get(), dispenseBehavior);
        ModBlockEntities.init();
        ModMenuTypes.init();
        ModCreativeModeTabs.register();
        ModEntities.init();
        ModSounds.init();
        AnvilSoundServices.register(new FabricAnvilSoundService());

        ToolTypeRegistry.init();
        ModAdvancementTriggers.register(net.fabricmc.fabric.api.object.builder.v1.advancement.CriterionRegistry::register);

        ReloadListenerRegistry.register();
        ModItemInteractEvents.register();
        ModEvents.register();
        ModMessages.register();

        if (FabricLoader.getInstance().isModLoaded("accessories")) {
            AttributeModifierHandler.register();
            Overgeared.LOGGER.info("Accessories mod detected - AttributeModifierHandler registered");
        } else {
            Overgeared.LOGGER.info("Accessories mod not present - skipping AttributeModifierHandler registration");
        }

        ModLootModifiers.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ModCommands.register(dispatcher));
    }
}
