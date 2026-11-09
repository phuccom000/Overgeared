package net.stirdrem.overgeared;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.registries.Registries;
import net.stirdrem.overgeared.platform.Services;
import net.stirdrem.overgeared.config.ModConfigs;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.UpgradeArrowDispenseBehavior;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.compat.ModCompat;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.item.ModCreativeModeTabs;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.recipe.ModRecipeSerializers;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.screen.ModMenuTypes;
import net.stirdrem.overgeared.sound.ModSounds;
import org.slf4j.Logger;

/**
 * Loader-independent entry point. The loader entry points call {@link #init()} while the mod is
 * being constructed and {@link #commonSetup()} once registries are frozen.
 */
public final class OvergearedMod {
    public static final String MOD_ID = "overgeared";
    public static final Logger LOGGER = LogUtils.getLogger();
    /**
     * Added to the enum by NeoForge's enum extensions (neoforge) or by a mixin (fabric).
     */

    private OvergearedMod() {
    }

    public static void init() {
        // Load configs first: registration and later setup read config values
        ModConfigs.load();

        // Order matters on Fabric, where entries are registered immediately.
        ModSounds.register();
        ModComponents.register();
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModEntities.register();
        ModMenuTypes.register();
        ModRecipeTypes.register();
        ModRecipeSerializers.register();
        ModCreativeModeTabs.register();
        ModAdvancementTriggers.register();
        registerAliases();

        ModCompat.init();
    }

    /** Ids renamed since earlier versions, so existing worlds keep their blocks, items and block entities. */
    private static void registerAliases() {
        // Before anvil tiers became configurable the iron anvil was "smithing_anvil" and the stone one "stone_anvil"
        Services.PLATFORM.registerAlias(Registries.BLOCK, loc("smithing_anvil"), loc("iron_smithing_anvil"));
        Services.PLATFORM.registerAlias(Registries.ITEM, loc("smithing_anvil"), loc("iron_smithing_anvil"));
        Services.PLATFORM.registerAlias(Registries.BLOCK, loc("stone_anvil"), loc("stone_smithing_anvil"));
        Services.PLATFORM.registerAlias(Registries.ITEM, loc("stone_anvil"), loc("stone_smithing_anvil"));
        // All tiered anvils now share the iron anvil's block entity type
        Services.PLATFORM.registerAlias(Registries.BLOCK_ENTITY_TYPE, loc("tier_a_smithing_table_be"), loc("smithing_table_be"));
        Services.PLATFORM.registerAlias(Registries.BLOCK_ENTITY_TYPE, loc("tier_b_smithing_table_be"), loc("smithing_table_be"));
        // The mod's own copper tools, armor and nugget were replaced by vanilla's (26.x)
        for (String piece : new String[]{"sword", "pickaxe", "axe", "hoe", "shovel", "helmet", "chestplate", "leggings", "boots", "nugget"}) {
            Services.PLATFORM.registerAlias(Registries.ITEM, loc("copper_" + piece), Identifier.withDefaultNamespace("copper_" + piece));
        }
    }

    public static void commonSetup() {
        ToolTypeRegistry.init();

        DispenserBlock.registerBehavior(ModItems.LINGERING_ARROW.get(), new UpgradeArrowDispenseBehavior());
        DispenserBlock.registerBehavior(ModItems.IRON_UPGRADE_ARROW.get(), new UpgradeArrowDispenseBehavior());
        DispenserBlock.registerBehavior(ModItems.STEEL_UPGRADE_ARROW.get(), new UpgradeArrowDispenseBehavior());
        DispenserBlock.registerBehavior(ModItems.DIAMOND_UPGRADE_ARROW.get(), new UpgradeArrowDispenseBehavior());
    }

    public static Identifier loc(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }
}
