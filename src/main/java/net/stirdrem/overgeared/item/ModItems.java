package net.stirdrem.overgeared.item;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.item.custom.*;
import net.stirdrem.overgeared.util.ModTags;

import java.util.function.Function;

public class ModItems {
    // Copper tools, copper armor and the copper nugget are vanilla items since 26.x; Overgeared's
    // old copies were removed (see registerLegacyAliases) and its copper forging recipes now
    // produce the vanilla items.


    public static final Item CRUDE_STEEL = register("crude_steel", Item::new, new Item.Properties());

    public static final Item HEATED_CRUDE_STEEL = register("heated_crude_steel", Item::new, new Item.Properties());

    public static final Item ROCK = register("knappable_rock", Item::new, new Item.Properties());

    public static final Item STEEL_INGOT = register("steel_ingot", Item::new, new Item.Properties());

    public static final Item NETHERITE_ALLOY = register("netherite_alloy", Item::new, new Item.Properties());

    public static final Item UNFIRED_TOOL_CAST = register("unfired_tool_cast",
            p -> new ToolCastItem(false, false, p), new Item.Properties());

    public static final Item CLAY_TOOL_CAST = register("clay_tool_cast",
            p -> new ToolCastItem(true, true, p), new Item.Properties().stacksTo(1));

    public static final Item NETHER_TOOL_CAST = register("nether_tool_cast",
            p -> new ToolCastItem(true, false, p), new Item.Properties().stacksTo(1));

    public static final Item STEEL_NUGGET = register("steel_nugget", Item::new, new Item.Properties());


    public static final Item IRON_ARROW_HEAD = register("iron_arrow_head", Item::new, new Item.Properties());

    public static final Item STEEL_ARROW_HEAD = register("steel_arrow_head", Item::new, new Item.Properties());

    public static final Item DIAMOND_SHARD = register("diamond_shard", Item::new, new Item.Properties());

    public static final Item HEATED_IRON_INGOT = register("heated_iron_ingot", Item::new, new Item.Properties());

    public static final Item HEATED_COPPER_INGOT = register("heated_copper_ingot", Item::new, new Item.Properties());

    public static final Item HEATED_STEEL_INGOT = register("heated_steel_ingot", Item::new, new Item.Properties());

    public static final Item HEATED_SILVER_INGOT = register("heated_silver_ingot", Item::new, new Item.Properties());

    public static final Item HEATED_NETHERITE_ALLOY = register("heated_netherite_alloy", Item::new, new Item.Properties());

    public static final Item COPPER_PLATE = register("copper_plate", Item::new, new Item.Properties());

    public static final Item IRON_PLATE = register("iron_plate", Item::new, new Item.Properties());

    public static final Item STEEL_PLATE = register("steel_plate", Item::new, new Item.Properties());

    public static final Item STEEL_TONG = register("steel_tong", Item::new, new Item.Properties());
    public static final Item IRON_TONG = register("iron_tong", Item::new, new Item.Properties());
    public static final Item WOODEN_TONGS = register("wooden_tongs",
            Tongs::new, new Item.Properties().tool(ToolMaterial.WOOD, ModTags.Blocks.SMITHING, -1, -2f, 0).durability(120));
    public static final Item IRON_TONGS = register("iron_tongs",
            Tongs::new, new Item.Properties().tool(ToolMaterial.IRON, ModTags.Blocks.SMITHING, -1, -2f, 0).durability(512));

    public static final Item STEEL_TONGS = register("steel_tongs",
            Tongs::new, new Item.Properties().tool(ModToolTiers.STEEL, ModTags.Blocks.SMITHING, -1, -2f, 0).durability(1024));

    public static final Item STONE_HAMMER_HEAD = register("stone_hammer_head", Item::new, new Item.Properties());

    public static final Item COPPER_HAMMER_HEAD = register("copper_hammer_head", Item::new, new Item.Properties());

    public static final Item STEEL_HAMMER_HEAD = register("steel_hammer_head", Item::new, new Item.Properties());

    public static final Item SMITHING_HAMMER = register("smithing_hammer",
            SmithingHammer::new, new Item.Properties().tool(ModToolTiers.STEEL, ModTags.Blocks.SMITHING, -1, -2.8f, 0));

    public static final Item COPPER_SMITHING_HAMMER = register("copper_smithing_hammer",
            SmithingHammer::new, new Item.Properties().tool(ToolMaterial.COPPER, ModTags.Blocks.SMITHING, -1, -2.8f, 0));

    public static final Item DIAMOND_UPGRADE_SMITHING_TEMPLATE = register("diamond_upgrade_smithing_template",
            DiamondUpgradeTemplateItem::createDiamondUpgradeTemplate, new Item.Properties());

    public static final Item EMPTY_BLUEPRINT = register("empty_blueprint", Item::new, new Item.Properties());

    public static final Item BLUEPRINT = register("blueprint",
            BlueprintItem::new, new Item.Properties());

    public static final Item STONE_SWORD_BLADE = register("stone_sword_blade", Item::new, new Item.Properties());
    public static final Item IRON_SWORD_BLADE = register("iron_sword_blade", Item::new, new Item.Properties());
    public static final Item GOLDEN_SWORD_BLADE = register("golden_sword_blade", Item::new, new Item.Properties());
    public static final Item STEEL_SWORD_BLADE = register("steel_sword_blade", Item::new, new Item.Properties());
    public static final Item COPPER_SWORD_BLADE = register("copper_sword_blade", Item::new, new Item.Properties());

    public static final Item STONE_PICKAXE_HEAD = register("stone_pickaxe_head", Item::new, new Item.Properties());
    public static final Item IRON_PICKAXE_HEAD = register("iron_pickaxe_head", Item::new, new Item.Properties());
    public static final Item GOLDEN_PICKAXE_HEAD = register("golden_pickaxe_head", Item::new, new Item.Properties());
    public static final Item STEEL_PICKAXE_HEAD = register("steel_pickaxe_head", Item::new, new Item.Properties());
    public static final Item COPPER_PICKAXE_HEAD = register("copper_pickaxe_head", Item::new, new Item.Properties());


    public static final Item STONE_AXE_HEAD = register("stone_axe_head", Item::new, new Item.Properties());
    public static final Item IRON_AXE_HEAD = register("iron_axe_head", Item::new, new Item.Properties());
    public static final Item GOLDEN_AXE_HEAD = register("golden_axe_head", Item::new, new Item.Properties());
    public static final Item STEEL_AXE_HEAD = register("steel_axe_head", Item::new, new Item.Properties());
    public static final Item COPPER_AXE_HEAD = register("copper_axe_head", Item::new, new Item.Properties());


    public static final Item STONE_SHOVEL_HEAD = register("stone_shovel_head", Item::new, new Item.Properties());
    public static final Item IRON_SHOVEL_HEAD = register("iron_shovel_head", Item::new, new Item.Properties());
    public static final Item GOLDEN_SHOVEL_HEAD = register("golden_shovel_head", Item::new, new Item.Properties());
    public static final Item STEEL_SHOVEL_HEAD = register("steel_shovel_head", Item::new, new Item.Properties());
    public static final Item COPPER_SHOVEL_HEAD = register("copper_shovel_head", Item::new, new Item.Properties());

    public static final Item STONE_SPEAR_HEAD = register("stone_spear_head", Item::new, new Item.Properties());
    public static final Item IRON_SPEAR_HEAD = register("iron_spear_head", Item::new, new Item.Properties());
    public static final Item GOLDEN_SPEAR_HEAD = register("golden_spear_head", Item::new, new Item.Properties());
    public static final Item STEEL_SPEAR_HEAD = register("steel_spear_head", Item::new, new Item.Properties());
    public static final Item COPPER_SPEAR_HEAD = register("copper_spear_head", Item::new, new Item.Properties());


    public static final Item STONE_HOE_HEAD = register("stone_hoe_head", Item::new, new Item.Properties());
    public static final Item IRON_HOE_HEAD = register("iron_hoe_head", Item::new, new Item.Properties());
    public static final Item GOLDEN_HOE_HEAD = register("golden_hoe_head", Item::new, new Item.Properties());
    public static final Item STEEL_HOE_HEAD = register("steel_hoe_head", Item::new, new Item.Properties());
    public static final Item COPPER_HOE_HEAD = register("copper_hoe_head", Item::new, new Item.Properties());


    public static final Item STEEL_SWORD = register("steel_sword",
            Item::new, new Item.Properties().sword(ModToolTiers.STEEL, 3f, -2.4f));
    public static final Item STEEL_PICKAXE = register("steel_pickaxe",
            Item::new, new Item.Properties().pickaxe(ModToolTiers.STEEL, 1f, -2.8f));
    public static final Item STEEL_AXE = register("steel_axe",
            Item::new, new Item.Properties().axe(ModToolTiers.STEEL, 5f, -3f));
    public static final Item STEEL_HOE = register("steel_hoe",
            Item::new, new Item.Properties().hoe(ModToolTiers.STEEL, -3f, -0.5f));
    public static final Item STEEL_SHOVEL = register("steel_shovel",
            Item::new, new Item.Properties().shovel(ModToolTiers.STEEL, 1f, -3));
    // 26.x spear. Spear tuning (attack duration, damage multiplier, charge timings) sits between
    // vanilla's iron and diamond spears, like the rest of the steel tier.
    public static final Item STEEL_SPEAR = register("steel_spear",
            Item::new, new Item.Properties().spear(ModToolTiers.STEEL, 1.0F, 1.0125F, 0.55F, 2.75F, 10.5F, 6.625F, 5.1F, 10.625F, 4.6F));

    public static final Item STEEL_HELMET = register("steel_helmet",
            Item::new, new Item.Properties().humanoidArmor(ModArmorMaterials.STEEL, ArmorType.HELMET));
    public static final Item STEEL_CHESTPLATE = register("steel_chestplate",
            Item::new, new Item.Properties().humanoidArmor(ModArmorMaterials.STEEL, ArmorType.CHESTPLATE));
    public static final Item STEEL_LEGGINGS = register("steel_leggings",
            Item::new, new Item.Properties().humanoidArmor(ModArmorMaterials.STEEL, ArmorType.LEGGINGS));
    public static final Item STEEL_BOOTS = register("steel_boots",
            Item::new, new Item.Properties().humanoidArmor(ModArmorMaterials.STEEL, ArmorType.BOOTS));



    public static final Item LINGERING_ARROW = register("lingering_arrow",
            p -> new LingeringArrowItem(p, ArrowTier.FLINT), arrowProperties());

    public static final Item IRON_UPGRADE_ARROW = register("iron_arrow",
            p -> new UpgradeArrowItem(p, ArrowTier.IRON), arrowProperties());
    public static final Item STEEL_UPGRADE_ARROW = register("steel_arrow",
            p -> new UpgradeArrowItem(p, ArrowTier.STEEL), arrowProperties());
    public static final Item DIAMOND_UPGRADE_ARROW = register("diamond_arrow",
            p -> new UpgradeArrowItem(p, ArrowTier.DIAMOND), arrowProperties());


    // Potion tooltips on tipped/lingering arrows are scaled like vanilla tipped arrows.
    private static Item.Properties arrowProperties() {
        return new Item.Properties().component(DataComponents.POTION_DURATION_SCALE, 0.125F);
    }

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Overgeared.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
    }

    public static void register() {
        registerLegacyAliases();
    }

    /** Maps the removed overgeared:copper_* ids to vanilla so items in existing worlds convert instead of vanishing. */
    private static void registerLegacyAliases() {
        for (String name : new String[]{"copper_nugget", "copper_helmet", "copper_chestplate", "copper_leggings",
                "copper_boots", "copper_sword", "copper_pickaxe", "copper_axe", "copper_hoe", "copper_shovel"}) {
            BuiltInRegistries.ITEM.addAlias(Overgeared.id(name), net.minecraft.resources.Identifier.withDefaultNamespace(name));
        }
    }
}
