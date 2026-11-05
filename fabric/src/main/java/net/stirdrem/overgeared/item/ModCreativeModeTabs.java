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
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;

public class ModCreativeModeTabs {

    public static final CreativeModeTab OVERGEARED_TAB = register("overgeared_tab",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.IRON_TONGS.get()))
                    .title(Component.translatable("creativetab.overgeared_tab"))
                    .displayItems((displayContext, entries) -> {
                        // General materials/tools
                        entries.accept(ModItems.CRUDE_STEEL.get());
                        entries.accept(ModItems.HEATED_CRUDE_STEEL.get());
                        entries.accept(ModItems.ROCK.get());
                        entries.accept(ModItems.COPPER_NUGGET.get());
                        entries.accept(ModItems.STEEL_INGOT.get());
                        entries.accept(ModItems.STEEL_NUGGET.get());
                        entries.accept(ModItems.IRON_ARROW_HEAD.get());
                        entries.accept(ModItems.STEEL_ARROW_HEAD.get());
                        entries.accept(ModItems.DIAMOND_SHARD.get());
                        entries.accept(ModItems.IRON_UPGRADE_ARROW.get());
                        entries.accept(ModItems.STEEL_UPGRADE_ARROW.get());
                        entries.accept(ModItems.DIAMOND_UPGRADE_ARROW.get());
                        entries.accept(ModItems.HEATED_COPPER_INGOT.get());
                        entries.accept(ModItems.HEATED_IRON_INGOT.get());
                        entries.accept(ModItems.HEATED_SILVER_INGOT.get());
                        entries.accept(ModItems.HEATED_STEEL_INGOT.get());
                        entries.accept(ModItems.NETHERITE_ALLOY.get());
                        entries.accept(ModItems.HEATED_NETHERITE_ALLOY.get());
                        entries.accept(ModItems.COPPER_PLATE.get());
                        entries.accept(ModItems.IRON_PLATE.get());
                        entries.accept(ModItems.STEEL_PLATE.get());
                        entries.accept(ModItems.IRON_TONG.get());
                        entries.accept(ModItems.STEEL_TONG.get());
                        entries.accept(ModItems.WOODEN_TONGS.get());
                        entries.accept(ModItems.IRON_TONGS.get());
                        entries.accept(ModItems.STEEL_TONGS.get());
                        entries.accept(ModItems.STONE_HAMMER_HEAD.get());
                        entries.accept(ModItems.COPPER_HAMMER_HEAD.get());
                        entries.accept(ModItems.STEEL_HAMMER_HEAD.get());
                        entries.accept(ModItems.COPPER_SMITHING_HAMMER.get());
                        entries.accept(ModItems.SMITHING_HAMMER.get());
                        entries.accept(ModItems.EMPTY_BLUEPRINT.get());
                        entries.accept(ModItems.BLUEPRINT.get());
                        entries.accept(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get());
                        entries.accept(ModItems.UNFIRED_TOOL_CAST.get());
                        entries.accept(ModItems.CLAY_TOOL_CAST.get());
                        entries.accept(ModItems.NETHER_TOOL_CAST.get());
                        entries.accept(ModItems.COPPER_HELMET.get());
                        entries.accept(ModItems.COPPER_CHESTPLATE.get());
                        entries.accept(ModItems.COPPER_LEGGINGS.get());
                        entries.accept(ModItems.COPPER_BOOTS.get());

                        entries.accept(ModItems.STEEL_HELMET.get());
                        entries.accept(ModItems.STEEL_CHESTPLATE.get());
                        entries.accept(ModItems.STEEL_LEGGINGS.get());
                        entries.accept(ModItems.STEEL_BOOTS.get());

                        entries.accept(ModItems.COPPER_SWORD.get());
                        entries.accept(ModItems.COPPER_PICKAXE.get());
                        entries.accept(ModItems.COPPER_AXE.get());
                        entries.accept(ModItems.COPPER_SHOVEL.get());
                        entries.accept(ModItems.COPPER_HOE.get());

                        entries.accept(ModItems.STEEL_SWORD.get());
                        entries.accept(ModItems.STEEL_PICKAXE.get());
                        entries.accept(ModItems.STEEL_AXE.get());
                        entries.accept(ModItems.STEEL_SHOVEL.get());
                        entries.accept(ModItems.STEEL_HOE.get());

                        // === STONE ===
                        entries.accept(ModItems.STONE_SWORD_BLADE.get());
                        entries.accept(ModItems.STONE_PICKAXE_HEAD.get());
                        entries.accept(ModItems.STONE_AXE_HEAD.get());
                        entries.accept(ModItems.STONE_SHOVEL_HEAD.get());
                        entries.accept(ModItems.STONE_HOE_HEAD.get());

                        // === COPPER ===
                        entries.accept(ModItems.COPPER_SWORD_BLADE.get());
                        entries.accept(ModItems.COPPER_PICKAXE_HEAD.get());
                        entries.accept(ModItems.COPPER_AXE_HEAD.get());
                        entries.accept(ModItems.COPPER_SHOVEL_HEAD.get());
                        entries.accept(ModItems.COPPER_HOE_HEAD.get());

                        // === IRON ===
                        entries.accept(ModItems.IRON_SWORD_BLADE.get());
                        entries.accept(ModItems.IRON_PICKAXE_HEAD.get());
                        entries.accept(ModItems.IRON_AXE_HEAD.get());
                        entries.accept(ModItems.IRON_SHOVEL_HEAD.get());
                        entries.accept(ModItems.IRON_HOE_HEAD.get());

                        // === GOLD ===
                        entries.accept(ModItems.GOLDEN_SWORD_BLADE.get());
                        entries.accept(ModItems.GOLDEN_PICKAXE_HEAD.get());
                        entries.accept(ModItems.GOLDEN_AXE_HEAD.get());
                        entries.accept(ModItems.GOLDEN_SHOVEL_HEAD.get());
                        entries.accept(ModItems.GOLDEN_HOE_HEAD.get());

                        // === STEEL ===
                        entries.accept(ModItems.STEEL_SWORD_BLADE.get());
                        entries.accept(ModItems.STEEL_PICKAXE_HEAD.get());
                        entries.accept(ModItems.STEEL_AXE_HEAD.get());
                        entries.accept(ModItems.STEEL_SHOVEL_HEAD.get());
                        entries.accept(ModItems.STEEL_HOE_HEAD.get());

                        for (AnvilTier tier : AnvilTier.all()) {
                            if (tier.isEnabled()) entries.accept(ModBlocks.anvilFor(tier));
                        }
                        entries.accept(ModBlocks.STEEL_BLOCK.get());
                        entries.accept(ModBlocks.DRAFTING_TABLE.get());
                        entries.accept(ModBlocks.ALLOY_FURNACE.get());
                        entries.accept(ModBlocks.NETHER_ALLOY_FURNACE.get());
                        entries.accept(ModBlocks.CAST_FURNACE.get());
                    })
                    .build());

    public static final CreativeModeTab LINGERING_ARROWS_TAB = register("lingering_arrows_tab",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(Blocks.FLETCHING_TABLE))
                    .title(Component.translatable("creativetab.overgeared.lingering_arrows_tab"))
                    .displayItems((displayContext, entries) -> {
                        if (!Overgeared.getConfig().ENABLE_FLETCHING_RECIPES())
                            return;

                        entries.accept(Items.ARROW);
                        entries.accept(ModItems.IRON_UPGRADE_ARROW.get());
                        entries.accept(ModItems.STEEL_UPGRADE_ARROW.get());
                        entries.accept(ModItems.DIAMOND_UPGRADE_ARROW.get());

                        for (Potion potion : BuiltInRegistries.POTION) {
                            if (potion == Potions.EMPTY)
                                continue;

                            ItemStack arrow = new ItemStack(Items.TIPPED_ARROW);
                            arrow.getOrCreateTag().putString("Potion",
                                    BuiltInRegistries.POTION.getKey(potion).toString());
                            entries.accept(arrow);
                        }

                        for (Potion potion : BuiltInRegistries.POTION) {
                            if (potion == Potions.EMPTY)
                                continue;

                            ItemStack arrow = new ItemStack(ModItems.LINGERING_ARROW.get());
                            arrow.getOrCreateTag().putString("Potion",
                                    BuiltInRegistries.POTION.getKey(potion).toString());
                            entries.accept(arrow);
                        }

                        for (Potion potion : BuiltInRegistries.POTION) {
                            if (potion == Potions.EMPTY)
                                continue;

                            ItemStack iron = new ItemStack(ModItems.IRON_UPGRADE_ARROW.get());
                            iron.getOrCreateTag().putString("Potion",
                                    BuiltInRegistries.POTION.getKey(potion).toString());
                            entries.accept(iron);

                            ItemStack ironLingering = iron.copy();
                            ironLingering.getOrCreateTag().putBoolean("LingeringPotion", true);
                            entries.accept(ironLingering);
                        }

                        for (Potion potion : BuiltInRegistries.POTION) {
                            if (potion == Potions.EMPTY)
                                continue;

                            ItemStack steel = new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get());
                            steel.getOrCreateTag().putString("Potion",
                                    BuiltInRegistries.POTION.getKey(potion).toString());
                            entries.accept(steel);

                            ItemStack steelLingering = steel.copy();
                            steelLingering.getOrCreateTag().putBoolean("LingeringPotion", true);
                            entries.accept(steelLingering);
                        }

                        for (Potion potion : BuiltInRegistries.POTION) {
                            if (potion == Potions.EMPTY)
                                continue;

                            ItemStack diamond = new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW.get());
                            diamond.getOrCreateTag().putString("Potion",
                                    BuiltInRegistries.POTION.getKey(potion).toString());
                            entries.accept(diamond);

                            ItemStack diamondLingering = diamond.copy();
                            diamondLingering.getOrCreateTag().putBoolean("LingeringPotion", true);
                            entries.accept(diamondLingering);
                        }
                    })
                    .build());

    public static final CreativeModeTab BLUEPRINT_TAB = register("blueprint_tab",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.BLUEPRINT.get()))
                    .title(Component.translatable("creativetab.overgeared.blueprint_tab"))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(ModItems.EMPTY_BLUEPRINT.get());
                        for (ToolType toolType : ToolTypeRegistry.getRegisteredTypesAll()) {
                            for (ForgingQuality quality : ForgingQuality.tiers()) {
                                ItemStack blueprint = new ItemStack(ModItems.BLUEPRINT.get());
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
