package net.stirdrem.overgeared;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.level.block.DispenserBlock;
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
    public static final RecipeBookType FORGING = RecipeBookType.valueOf("OVERGEARED_FORGING");

    private OvergearedMod() {
    }

    public static void init() {
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

        ModCompat.init();
    }

    public static void commonSetup() {
        ToolTypeRegistry.init();

        DispenserBlock.registerBehavior(ModItems.LINGERING_ARROW.get(), new UpgradeArrowDispenseBehavior());
        DispenserBlock.registerBehavior(ModItems.IRON_UPGRADE_ARROW.get(), new UpgradeArrowDispenseBehavior());
        DispenserBlock.registerBehavior(ModItems.STEEL_UPGRADE_ARROW.get(), new UpgradeArrowDispenseBehavior());
        DispenserBlock.registerBehavior(ModItems.DIAMOND_UPGRADE_ARROW.get(), new UpgradeArrowDispenseBehavior());
    }

    public static ResourceLocation loc(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
