package net.stirdrem.overgeared.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.config.ServerConfig;

public class ModCreativeModeTabs {

    public static final CreativeModeTab OVERGEARED_TAB = register("overgeared_tab",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.IRON_TONGS))
                    .title(Component.translatable("creativetab.overgeared_tab"))
                    .displayItems((displayContext, entries) -> {
                        // General materials/tools
                        entries.accept(ModItems.CRUDE_STEEL);
                        entries.accept(ModItems.HEATED_CRUDE_STEEL);
                        entries.accept(ModItems.ROCK);
                        entries.accept(ModItems.COPPER_NUGGET);
                        entries.accept(ModItems.STEEL_INGOT);
                        entries.accept(ModItems.STEEL_NUGGET);
                        entries.accept(ModItems.IRON_ARROW_HEAD);
                        entries.accept(ModItems.STEEL_ARROW_HEAD);
                        entries.accept(ModItems.DIAMOND_SHARD);
                        entries.accept(ModItems.IRON_UPGRADE_ARROW);
                        entries.accept(ModItems.STEEL_UPGRADE_ARROW);
                        entries.accept(ModItems.DIAMOND_UPGRADE_ARROW);
                        entries.accept(ModItems.HEATED_COPPER_INGOT);
                        entries.accept(ModItems.HEATED_IRON_INGOT);
                        entries.accept(ModItems.HEATED_SILVER_INGOT);
                        entries.accept(ModItems.HEATED_STEEL_INGOT);
                        entries.accept(ModItems.NETHERITE_ALLOY);
                        entries.accept(ModItems.HEATED_NETHERITE_ALLOY);
                        entries.accept(ModItems.COPPER_PLATE);
                        entries.accept(ModItems.IRON_PLATE);
                        entries.accept(ModItems.STEEL_PLATE);
                        entries.accept(ModItems.IRON_TONG);
                        entries.accept(ModItems.STEEL_TONG);
                        entries.accept(ModItems.WOODEN_TONGS);
                        entries.accept(ModItems.IRON_TONGS);
                        entries.accept(ModItems.STEEL_TONGS);
                        entries.accept(ModItems.STONE_HAMMER_HEAD);
                        entries.accept(ModItems.COPPER_HAMMER_HEAD);
                        entries.accept(ModItems.STEEL_HAMMER_HEAD);
                        entries.accept(ModItems.COPPER_SMITHING_HAMMER);
                        entries.accept(ModItems.SMITHING_HAMMER);
                        entries.accept(ModItems.EMPTY_BLUEPRINT);
                        entries.accept(ModItems.BLUEPRINT);
                        entries.accept(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE);
                        entries.accept(ModItems.UNFIRED_TOOL_CAST);
                        entries.accept(ModItems.CLAY_TOOL_CAST);
                        entries.accept(ModItems.NETHER_TOOL_CAST);
                        entries.accept(ModItems.COPPER_HELMET);
                        entries.accept(ModItems.COPPER_CHESTPLATE);
                        entries.accept(ModItems.COPPER_LEGGINGS);
                        entries.accept(ModItems.COPPER_BOOTS);

                        entries.accept(ModItems.STEEL_HELMET);
                        entries.accept(ModItems.STEEL_CHESTPLATE);
                        entries.accept(ModItems.STEEL_LEGGINGS);
                        entries.accept(ModItems.STEEL_BOOTS);

                        entries.accept(ModItems.COPPER_SWORD);
                        entries.accept(ModItems.COPPER_PICKAXE);
                        entries.accept(ModItems.COPPER_AXE);
                        entries.accept(ModItems.COPPER_SHOVEL);
                        entries.accept(ModItems.COPPER_HOE);

                        entries.accept(ModItems.STEEL_SWORD);
                        entries.accept(ModItems.STEEL_PICKAXE);
                        entries.accept(ModItems.STEEL_AXE);
                        entries.accept(ModItems.STEEL_SHOVEL);
                        entries.accept(ModItems.STEEL_HOE);

                        // === STONE ===
                        entries.accept(ModItems.STONE_SWORD_BLADE);
                        entries.accept(ModItems.STONE_PICKAXE_HEAD);
                        entries.accept(ModItems.STONE_AXE_HEAD);
                        entries.accept(ModItems.STONE_SHOVEL_HEAD);
                        entries.accept(ModItems.STONE_HOE_HEAD);

                        // === COPPER ===
                        entries.accept(ModItems.COPPER_SWORD_BLADE);
                        entries.accept(ModItems.COPPER_PICKAXE_HEAD);
                        entries.accept(ModItems.COPPER_AXE_HEAD);
                        entries.accept(ModItems.COPPER_SHOVEL_HEAD);
                        entries.accept(ModItems.COPPER_HOE_HEAD);

                        // === IRON ===
                        entries.accept(ModItems.IRON_SWORD_BLADE);
                        entries.accept(ModItems.IRON_PICKAXE_HEAD);
                        entries.accept(ModItems.IRON_AXE_HEAD);
                        entries.accept(ModItems.IRON_SHOVEL_HEAD);
                        entries.accept(ModItems.IRON_HOE_HEAD);

                        // === GOLD ===
                        entries.accept(ModItems.GOLDEN_SWORD_BLADE);
                        entries.accept(ModItems.GOLDEN_PICKAXE_HEAD);
                        entries.accept(ModItems.GOLDEN_AXE_HEAD);
                        entries.accept(ModItems.GOLDEN_SHOVEL_HEAD);
                        entries.accept(ModItems.GOLDEN_HOE_HEAD);

                        // === STEEL ===
                        entries.accept(ModItems.STEEL_SWORD_BLADE);
                        entries.accept(ModItems.STEEL_PICKAXE_HEAD);
                        entries.accept(ModItems.STEEL_AXE_HEAD);
                        entries.accept(ModItems.STEEL_SHOVEL_HEAD);
                        entries.accept(ModItems.STEEL_HOE_HEAD);

                        entries.accept(ModBlocks.STONE_SMITHING_ANVIL);
                        entries.accept(ModBlocks.SMITHING_ANVIL);
                        if (ServerConfig.ENABLE_TIER_A.get())
                            entries.accept(ModBlocks.TIER_A_SMITHING_ANVIL);
                        if (ServerConfig.ENABLE_TIER_B.get())
                            entries.accept(ModBlocks.TIER_B_SMITHING_ANVIL);
                        entries.accept(ModBlocks.STEEL_BLOCK);
                        entries.accept(ModBlocks.DRAFTING_TABLE);
                        entries.accept(ModBlocks.ALLOY_FURNACE);
                        entries.accept(ModBlocks.NETHER_ALLOY_FURNACE);
                        entries.accept(ModBlocks.CAST_FURNACE);
                    })
                    .build());

    public static final CreativeModeTab LINGERING_ARROWS_TAB = register("lingering_arrows_tab",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(Blocks.FLETCHING_TABLE))
                    .title(Component.translatable("creativetab.overgeared.lingering_arrows_tab"))
                    .displayItems((displayContext, entries) -> {
                        if (!ServerConfig.ENABLE_FLETCHING_RECIPES.get()) return;

                        entries.accept(Items.ARROW);
                        entries.accept(ModItems.IRON_UPGRADE_ARROW);
                        entries.accept(ModItems.STEEL_UPGRADE_ARROW);
                        entries.accept(ModItems.DIAMOND_UPGRADE_ARROW);

                        for (Potion potion : BuiltInRegistries.POTION) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack arrow = new ItemStack(Items.TIPPED_ARROW);
                            arrow.getOrCreateTag().putString("Potion", BuiltInRegistries.POTION.getKey(potion).toString());
                            entries.accept(arrow);
                        }

                        for (Potion potion : BuiltInRegistries.POTION) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack arrow = new ItemStack(ModItems.LINGERING_ARROW);
                            arrow.getOrCreateTag().putString("Potion", BuiltInRegistries.POTION.getKey(potion).toString());
                            entries.accept(arrow);
                        }

                        for (Potion potion : BuiltInRegistries.POTION) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack iron = new ItemStack(ModItems.IRON_UPGRADE_ARROW);
                            iron.getOrCreateTag().putString("Potion", BuiltInRegistries.POTION.getKey(potion).toString());
                            entries.accept(iron);

                            ItemStack ironLingering = iron.copy();
                            ironLingering.getOrCreateTag().putBoolean("LingeringPotion", true);
                            entries.accept(ironLingering);
                        }

                        for (Potion potion : BuiltInRegistries.POTION) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack steel = new ItemStack(ModItems.STEEL_UPGRADE_ARROW);
                            steel.getOrCreateTag().putString("Potion", BuiltInRegistries.POTION.getKey(potion).toString());
                            entries.accept(steel);

                            ItemStack steelLingering = steel.copy();
                            steelLingering.getOrCreateTag().putBoolean("LingeringPotion", true);
                            entries.accept(steelLingering);
                        }

                        for (Potion potion : BuiltInRegistries.POTION) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack diamond = new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW);
                            diamond.getOrCreateTag().putString("Potion", BuiltInRegistries.POTION.getKey(potion).toString());
                            entries.accept(diamond);

                            ItemStack diamondLingering = diamond.copy();
                            diamondLingering.getOrCreateTag().putBoolean("LingeringPotion", true);
                            entries.accept(diamondLingering);
                        }
                    })
                    .build());

    public static final CreativeModeTab BLUEPRINT_TAB = register("blueprint_tab",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.BLUEPRINT))
                    .title(Component.translatable("creativetab.overgeared.blueprint_tab"))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(ModItems.EMPTY_BLUEPRINT);
                        for (ToolType toolType : ToolTypeRegistry.getRegisteredTypesAll()) {
                            for (BlueprintQuality quality : BlueprintQuality.values()) {
                                ItemStack blueprint = new ItemStack(ModItems.BLUEPRINT);
                                CompoundTag tag = blueprint.getOrCreateTag();

                                tag.putString("ToolType", toolType.getId());
                                tag.putString("Quality", quality.name());
                                tag.putInt("Uses", 0);

                                blueprint.setTag(tag);
                                entries.accept(blueprint);
                            }
                        }
                    })
                    .build());

    private static CreativeModeTab register(String name, CreativeModeTab group) {
        return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Overgeared.id(name), group);
    }

    public static void register() {
    }
}
