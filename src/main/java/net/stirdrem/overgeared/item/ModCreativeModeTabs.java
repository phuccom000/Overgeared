package net.stirdrem.overgeared.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.config.ServerConfig;

public class ModCreativeModeTabs {

    public static final ItemGroup OVERGEARED_TAB = register("overgeared_tab",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.IRON_TONGS))
                    .displayName(Text.translatable("creativetab.overgeared_tab"))
                    .entries((displayContext, entries) -> {
                        // General materials/tools
                        entries.add(ModItems.CRUDE_STEEL);
                        entries.add(ModItems.HEATED_CRUDE_STEEL);
                        entries.add(ModItems.ROCK);
                        entries.add(ModItems.COPPER_NUGGET);
                        entries.add(ModItems.STEEL_INGOT);
                        entries.add(ModItems.STEEL_NUGGET);
                        entries.add(ModItems.IRON_ARROW_HEAD);
                        entries.add(ModItems.STEEL_ARROW_HEAD);
                        entries.add(ModItems.DIAMOND_SHARD);
                        entries.add(ModItems.IRON_UPGRADE_ARROW);
                        entries.add(ModItems.STEEL_UPGRADE_ARROW);
                        entries.add(ModItems.DIAMOND_UPGRADE_ARROW);
                        entries.add(ModItems.HEATED_COPPER_INGOT);
                        entries.add(ModItems.HEATED_IRON_INGOT);
                        entries.add(ModItems.HEATED_SILVER_INGOT);
                        entries.add(ModItems.HEATED_STEEL_INGOT);
                        entries.add(ModItems.NETHERITE_ALLOY);
                        entries.add(ModItems.HEATED_NETHERITE_ALLOY);
                        entries.add(ModItems.COPPER_PLATE);
                        entries.add(ModItems.IRON_PLATE);
                        entries.add(ModItems.STEEL_PLATE);
                        entries.add(ModItems.IRON_TONG);
                        entries.add(ModItems.STEEL_TONG);
                        entries.add(ModItems.WOODEN_TONGS);
                        entries.add(ModItems.IRON_TONGS);
                        entries.add(ModItems.STEEL_TONGS);
                        entries.add(ModItems.STONE_HAMMER_HEAD);
                        entries.add(ModItems.COPPER_HAMMER_HEAD);
                        entries.add(ModItems.STEEL_HAMMER_HEAD);
                        entries.add(ModItems.COPPER_SMITHING_HAMMER);
                        entries.add(ModItems.SMITHING_HAMMER);
                        entries.add(ModItems.EMPTY_BLUEPRINT);
                        entries.add(ModItems.BLUEPRINT);
                        entries.add(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE);
                        entries.add(ModItems.UNFIRED_TOOL_CAST);
                        entries.add(ModItems.CLAY_TOOL_CAST);
                        entries.add(ModItems.NETHER_TOOL_CAST);
                        entries.add(ModItems.COPPER_HELMET);
                        entries.add(ModItems.COPPER_CHESTPLATE);
                        entries.add(ModItems.COPPER_LEGGINGS);
                        entries.add(ModItems.COPPER_BOOTS);

                        entries.add(ModItems.STEEL_HELMET);
                        entries.add(ModItems.STEEL_CHESTPLATE);
                        entries.add(ModItems.STEEL_LEGGINGS);
                        entries.add(ModItems.STEEL_BOOTS);

                        entries.add(ModItems.COPPER_SWORD);
                        entries.add(ModItems.COPPER_PICKAXE);
                        entries.add(ModItems.COPPER_AXE);
                        entries.add(ModItems.COPPER_SHOVEL);
                        entries.add(ModItems.COPPER_HOE);

                        entries.add(ModItems.STEEL_SWORD);
                        entries.add(ModItems.STEEL_PICKAXE);
                        entries.add(ModItems.STEEL_AXE);
                        entries.add(ModItems.STEEL_SHOVEL);
                        entries.add(ModItems.STEEL_HOE);

                        // === STONE ===
                        entries.add(ModItems.STONE_SWORD_BLADE);
                        entries.add(ModItems.STONE_PICKAXE_HEAD);
                        entries.add(ModItems.STONE_AXE_HEAD);
                        entries.add(ModItems.STONE_SHOVEL_HEAD);
                        entries.add(ModItems.STONE_HOE_HEAD);

                        // === COPPER ===
                        entries.add(ModItems.COPPER_SWORD_BLADE);
                        entries.add(ModItems.COPPER_PICKAXE_HEAD);
                        entries.add(ModItems.COPPER_AXE_HEAD);
                        entries.add(ModItems.COPPER_SHOVEL_HEAD);
                        entries.add(ModItems.COPPER_HOE_HEAD);

                        // === IRON ===
                        entries.add(ModItems.IRON_SWORD_BLADE);
                        entries.add(ModItems.IRON_PICKAXE_HEAD);
                        entries.add(ModItems.IRON_AXE_HEAD);
                        entries.add(ModItems.IRON_SHOVEL_HEAD);
                        entries.add(ModItems.IRON_HOE_HEAD);

                        // === GOLD ===
                        entries.add(ModItems.GOLDEN_SWORD_BLADE);
                        entries.add(ModItems.GOLDEN_PICKAXE_HEAD);
                        entries.add(ModItems.GOLDEN_AXE_HEAD);
                        entries.add(ModItems.GOLDEN_SHOVEL_HEAD);
                        entries.add(ModItems.GOLDEN_HOE_HEAD);

                        // === STEEL ===
                        entries.add(ModItems.STEEL_SWORD_BLADE);
                        entries.add(ModItems.STEEL_PICKAXE_HEAD);
                        entries.add(ModItems.STEEL_AXE_HEAD);
                        entries.add(ModItems.STEEL_SHOVEL_HEAD);
                        entries.add(ModItems.STEEL_HOE_HEAD);

                        entries.add(ModBlocks.STONE_SMITHING_ANVIL);
                        entries.add(ModBlocks.SMITHING_ANVIL);
                        if (ServerConfig.ENABLE_TIER_A.get())
                            entries.add(ModBlocks.TIER_A_SMITHING_ANVIL);
                        if (ServerConfig.ENABLE_TIER_B.get())
                            entries.add(ModBlocks.TIER_B_SMITHING_ANVIL);
                        entries.add(ModBlocks.STEEL_BLOCK);
                        entries.add(ModBlocks.DRAFTING_TABLE);
                        entries.add(ModBlocks.ALLOY_FURNACE);
                        entries.add(ModBlocks.NETHER_ALLOY_FURNACE);
                        entries.add(ModBlocks.CAST_FURNACE);
                    })
                    .build());

    public static final ItemGroup LINGERING_ARROWS_TAB = register("lingering_arrows_tab",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(Blocks.FLETCHING_TABLE))
                    .displayName(Text.translatable("creativetab.overgeared.lingering_arrows_tab"))
                    .entries((displayContext, entries) -> {
                        if (!ServerConfig.ENABLE_FLETCHING_RECIPES.get()) return;

                        entries.add(Items.ARROW);
                        entries.add(ModItems.IRON_UPGRADE_ARROW);
                        entries.add(ModItems.STEEL_UPGRADE_ARROW);
                        entries.add(ModItems.DIAMOND_UPGRADE_ARROW);

                        for (Potion potion : Registries.POTION) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack arrow = new ItemStack(Items.TIPPED_ARROW);
                            arrow.getOrCreateNbt().putString("Potion", Registries.POTION.getId(potion).toString());
                            entries.add(arrow);
                        }

                        for (Potion potion : Registries.POTION) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack arrow = new ItemStack(ModItems.LINGERING_ARROW);
                            arrow.getOrCreateNbt().putString("Potion", Registries.POTION.getId(potion).toString());
                            entries.add(arrow);
                        }

                        for (Potion potion : Registries.POTION) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack iron = new ItemStack(ModItems.IRON_UPGRADE_ARROW);
                            iron.getOrCreateNbt().putString("Potion", Registries.POTION.getId(potion).toString());
                            entries.add(iron);

                            ItemStack ironLingering = iron.copy();
                            ironLingering.getOrCreateNbt().putBoolean("LingeringPotion", true);
                            entries.add(ironLingering);
                        }

                        for (Potion potion : Registries.POTION) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack steel = new ItemStack(ModItems.STEEL_UPGRADE_ARROW);
                            steel.getOrCreateNbt().putString("Potion", Registries.POTION.getId(potion).toString());
                            entries.add(steel);

                            ItemStack steelLingering = steel.copy();
                            steelLingering.getOrCreateNbt().putBoolean("LingeringPotion", true);
                            entries.add(steelLingering);
                        }

                        for (Potion potion : Registries.POTION) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack diamond = new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW);
                            diamond.getOrCreateNbt().putString("Potion", Registries.POTION.getId(potion).toString());
                            entries.add(diamond);

                            ItemStack diamondLingering = diamond.copy();
                            diamondLingering.getOrCreateNbt().putBoolean("LingeringPotion", true);
                            entries.add(diamondLingering);
                        }
                    })
                    .build());

    public static final ItemGroup BLUEPRINT_TAB = register("blueprint_tab",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.BLUEPRINT))
                    .displayName(Text.translatable("creativetab.overgeared.blueprint_tab"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.EMPTY_BLUEPRINT);
                        for (ToolType toolType : ToolTypeRegistry.getRegisteredTypesAll()) {
                            for (BlueprintQuality quality : BlueprintQuality.values()) {
                                ItemStack blueprint = new ItemStack(ModItems.BLUEPRINT);
                                NbtCompound tag = blueprint.getOrCreateNbt();

                                tag.putString("ToolType", toolType.getId());
                                tag.putString("Quality", quality.name());
                                tag.putInt("Uses", 0);

                                blueprint.setNbt(tag);
                                entries.add(blueprint);
                            }
                        }
                    })
                    .build());

    private static ItemGroup register(String name, ItemGroup group) {
        return Registry.register(Registries.ITEM_GROUP, Overgeared.id(name), group);
    }

    public static void register() {
    }
}
