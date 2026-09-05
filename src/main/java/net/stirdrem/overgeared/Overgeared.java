package net.stirdrem.overgeared;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.UpgradeArrowDispenseBehavior;
import net.stirdrem.overgeared.compat.accessories.AttributeModifierHandler;
import net.stirdrem.overgeared.loot.ModLootModifiers;
import net.stirdrem.overgeared.command.ModCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.stirdrem.overgeared.config.ClientConfig;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.recipe.CoolingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.ModRecipes;
import net.stirdrem.overgeared.sound.ModSounds;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class Overgeared implements ModInitializer {
    public static final String MOD_ID = "overgeared";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Nullable
    private static MinecraftServer server;

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
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
        LOGGER.info("Overgeared: initializing Fabric port");

        // Must run before any other registration: several classes (BlueprintQuality,
        // ModToolTiers, item constructors) read config values in static initializers,
        // which run the first time those classes are touched.
        ServerConfig.loadConfig(FabricLoader.getInstance().getConfigDir().resolve("overgeared-server.json"));
        ClientConfig.loadConfig(ClientConfig.CLIENT_CONFIG, FabricLoader.getInstance().getConfigDir().resolve("overgeared-client.json"));

        ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);

        // Force static init / registration for each registry class.
        ModItems.register();
        ModBlocks.register();
        ModRecipes.register();
        ModRecipeTypes.register();

        UpgradeArrowDispenseBehavior dispenseBehavior = new UpgradeArrowDispenseBehavior();
        DispenserBlock.registerBehavior(ModItems.LINGERING_ARROW, dispenseBehavior);
        DispenserBlock.registerBehavior(ModItems.IRON_UPGRADE_ARROW, dispenseBehavior);
        DispenserBlock.registerBehavior(ModItems.STEEL_UPGRADE_ARROW, dispenseBehavior);
        DispenserBlock.registerBehavior(ModItems.DIAMOND_UPGRADE_ARROW, dispenseBehavior);
        net.stirdrem.overgeared.block.entity.ModBlockEntities.register();
        net.stirdrem.overgeared.screen.ModMenuTypes.register();
        net.stirdrem.overgeared.item.ModCreativeModeTabs.register();
        ModEntities.register();
        ModSounds.register();

        ToolTypeRegistry.init();
        ModAdvancementTriggers.register();

        net.stirdrem.overgeared.event.ReloadListenerRegistry.register();
        ModItemInteractEvents.register();
        net.stirdrem.overgeared.event.ModEvents.register();
        ModMessages.register();

        if (FabricLoader.getInstance().isModLoaded("accessories")) {
            AttributeModifierHandler.register();
            LOGGER.info("Accessories mod detected - AttributeModifierHandler registered");
        } else {
            LOGGER.info("Accessories mod not present - skipping AttributeModifierHandler registration");
        }

        if (FabricLoader.getInstance().isModLoaded("polymorph")) {
            net.stirdrem.overgeared.compat.polymorph.PolymorphIntegration.register();
            LOGGER.info("Polymorph mod detected - anvil recipe-choice integration registered");
        } else {
            LOGGER.info("Polymorph mod not present - skipping anvil recipe-choice integration");
        }

        ModLootModifiers.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ModCommands.register(dispatcher));
    }

    @Nullable
    public static Item getCooledItem(@Nullable Item heatedItem, Level world) {
        if (heatedItem == null || world == null) return null;

        SimpleContainer container = new SimpleContainer(new ItemStack(heatedItem));

        Optional<CoolingRecipe> recipeOpt = world.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.COOLING_RECIPE)
                .stream()
                .filter(r -> r.matches(container, world))
                .findFirst();

        if (recipeOpt.isEmpty()) {
            return heatedItem;
        }

        CoolingRecipe recipe = recipeOpt.get();
        ItemStack result = recipe.getResultItem(world.registryAccess());
        return result.isEmpty() ? heatedItem : result.getItem();
    }

    public static boolean isDurabilityBlacklisted(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<? extends String> blacklist = ServerConfig.BASE_DURABILITY_BLACKLIST.get();

        for (String entry : blacklist) {
            if (entry.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(entry.substring(1));
                TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                if (stack.is(tag)) return true;
            } else {
                if (itemId != null && itemId.equals(ResourceLocation.tryParse(entry))) return true;
            }
        }
        return net.stirdrem.overgeared.datapack.DurabilityBlacklistReloadListener.isBlacklisted(stack);
    }
}
