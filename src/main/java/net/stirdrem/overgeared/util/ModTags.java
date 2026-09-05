package net.stirdrem.overgeared.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.stirdrem.overgeared.Overgeared;

public class ModTags {
    public static class Blocks {
        public static final TagKey<net.minecraft.world.level.block.Block> NEEDS_STEEL_TOOL = tag("needs_steel_tool");
        public static final TagKey<net.minecraft.world.level.block.Block> NEEDS_COPPER_TOOL = tag("needs_copper_tool");
        public static final TagKey<net.minecraft.world.level.block.Block> SMITHING = tag("smithing");
        public static final TagKey<net.minecraft.world.level.block.Block> SMITHING_ANVIL = tag("smithing_anvil");
        public static final TagKey<net.minecraft.world.level.block.Block> STONE_ANVIL_BASES = tag("stone_anvil_bases");
        public static final TagKey<net.minecraft.world.level.block.Block> IRON_ANVIL_BASES = tag("iron_anvil_bases");
        public static final TagKey<net.minecraft.world.level.block.Block> TIER_A_ANVIL_BASES = tag("tier_a_anvil_bases");
        public static final TagKey<net.minecraft.world.level.block.Block> TIER_B_ANVIL_BASES = tag("tier_b_anvil_bases");
        public static final TagKey<net.minecraft.world.level.block.Block> GRINDSTONES = tag("grindstones");

        private static TagKey<net.minecraft.world.level.block.Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, new ResourceLocation(Overgeared.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> TONGS = tag("tongs");
        public static final TagKey<Item> TOOL_PARTS = tag("tool_parts");
        public static final TagKey<Item> HEATED_METALS = tag("heated_metals");
        public static final TagKey<Item> HOT_ITEMS = tag("hot_items");
        public static final TagKey<Item> SMITHING_HAMMERS = tag("smithing_hammers");
        public static final TagKey<Item> TOOL_CAST = tag("tool_casts");
        public static final TagKey<Item> KNAPPABLE = tag("knappables");
        public static final TagKey<Item> IRON_PLATES = tag("iron_plates");
        public static final TagKey<Item> COPPER_PLATES = tag("copper_plates");
        public static final TagKey<Item> QUALITY_BLACKLIST = tag("quality_blacklist");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, new ResourceLocation(Overgeared.MOD_ID, name));
        }
    }
}
