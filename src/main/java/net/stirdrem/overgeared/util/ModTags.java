package net.stirdrem.overgeared.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.stirdrem.overgeared.Overgeared;

public class ModTags {
    public static class Blocks {
        public static final TagKey<net.minecraft.world.level.block.Block> SMITHING = tag("smithing");
        public static final TagKey<net.minecraft.world.level.block.Block> SMITHING_ANVIL = tag("smithing_anvil");
        public static final TagKey<net.minecraft.world.level.block.Block> STONE_ANVIL_BASES = tag("stone_anvil_bases");
        public static final TagKey<net.minecraft.world.level.block.Block> IRON_ANVIL_BASES = tag("iron_anvil_bases");
        public static final TagKey<net.minecraft.world.level.block.Block> TIER_A_ANVIL_BASES = tag("tier_a_anvil_bases");
        public static final TagKey<net.minecraft.world.level.block.Block> TIER_B_ANVIL_BASES = tag("tier_b_anvil_bases");
        public static final TagKey<net.minecraft.world.level.block.Block> GRINDSTONES = tag("grindstones");
        // Blocks steel tools can't harvest: diamond-tier blocks except obsidian / crying obsidian
        // (iron < steel < diamond, as in the original mod's needs_steel_tool tier).
        public static final TagKey<net.minecraft.world.level.block.Block> INCORRECT_FOR_STEEL_TOOL = tag("incorrect_for_steel_tool");

        private static TagKey<net.minecraft.world.level.block.Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> TONGS = tag("tongs");
        public static final TagKey<Item> TOOL_PARTS = tag("tool_parts");
        public static final TagKey<Item> HEATED_METALS = tag("heated_metals");
        public static final TagKey<Item> HOT_ITEMS = tag("hot_items");
        public static final TagKey<Item> SMITHING_HAMMERS = tag("smithing_hammers");
        public static final TagKey<Item> STONE_SMITHING_HAMMERS = tag("stone_smithing_hammers");
        public static final TagKey<Item> IRON_SMITHING_HAMMERS = tag("iron_smithing_hammers");
        public static final TagKey<Item> TIER_A_SMITHING_HAMMERS = tag("tier_a_smithing_hammers");
        public static final TagKey<Item> TIER_B_SMITHING_HAMMERS = tag("tier_b_smithing_hammers");
        public static final TagKey<Item> TOOL_CAST = tag("tool_casts");
        public static final TagKey<Item> KNAPPABLE = tag("knappables");
        public static final TagKey<Item> IRON_PLATES = tag("iron_plates");
        public static final TagKey<Item> COPPER_PLATES = tag("copper_plates");
        public static final TagKey<Item> QUALITY_BLACKLIST = tag("quality_blacklist");
        // Repair materials for ModToolTiers.STEEL / ModArmorMaterials.STEEL (26.x materials repair by tag)
        public static final TagKey<Item> STEEL_TOOL_MATERIALS = tag("steel_tool_materials");
        public static final TagKey<Item> REPAIRS_STEEL_ARMOR = tag("repairs_steel_armor");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, name));
        }
    }
}
