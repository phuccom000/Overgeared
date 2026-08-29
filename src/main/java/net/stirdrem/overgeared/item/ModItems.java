package net.stirdrem.overgeared.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.item.armor.CopperHelmet;
import net.stirdrem.overgeared.item.armor.CopperLeggings;
import net.stirdrem.overgeared.item.custom.*;

public class ModItems {

    public static final Item CRUDE_STEEL = register("crude_steel",
            new Item(new FabricItemSettings()));

    public static final Item HEATED_CRUDE_STEEL = register("heated_crude_steel",
            new Item(new FabricItemSettings()));

    public static final Item ROCK = register("knappable_rock",
            new Item(new FabricItemSettings()));

    public static final Item STEEL_INGOT = register("steel_ingot",
            new Item(new FabricItemSettings()));

    public static final Item NETHERITE_ALLOY = register("netherite_alloy",
            new Item(new FabricItemSettings()));

    public static final Item UNFIRED_TOOL_CAST = register("unfired_tool_cast",
            new ToolCastItem(false, false, new FabricItemSettings()));

    public static final Item CLAY_TOOL_CAST = register("clay_tool_cast",
            new ToolCastItem(true, true,
                    new FabricItemSettings().maxCount(1).maxDamage(1)));

    public static final Item NETHER_TOOL_CAST = register("nether_tool_cast",
            new ToolCastItem(true, false, new FabricItemSettings().maxCount(1)));

    public static final Item STEEL_NUGGET = register("steel_nugget",
            new Item(new FabricItemSettings()));

    public static final Item COPPER_NUGGET = register("copper_nugget",
            new Item(new FabricItemSettings()));

    public static final Item IRON_ARROW_HEAD = register("iron_arrow_head",
            new Item(new FabricItemSettings()));

    public static final Item STEEL_ARROW_HEAD = register("steel_arrow_head",
            new Item(new FabricItemSettings()));

    public static final Item DIAMOND_SHARD = register("diamond_shard",
            new Item(new FabricItemSettings()));

    public static final Item HEATED_IRON_INGOT = register("heated_iron_ingot",
            new Item(new FabricItemSettings()));

    public static final Item HEATED_COPPER_INGOT = register("heated_copper_ingot",
            new Item(new FabricItemSettings()));

    public static final Item HEATED_STEEL_INGOT = register("heated_steel_ingot",
            new Item(new FabricItemSettings()));

    public static final Item HEATED_SILVER_INGOT = register("heated_silver_ingot",
            new Item(new FabricItemSettings()));

    public static final Item HEATED_NETHERITE_ALLOY = register("heated_netherite_alloy",
            new Item(new FabricItemSettings()));

    public static final Item COPPER_PLATE = register("copper_plate",
            new Item(new FabricItemSettings()));

    public static final Item IRON_PLATE = register("iron_plate",
            new Item(new FabricItemSettings()));

    public static final Item STEEL_PLATE = register("steel_plate",
            new Item(new FabricItemSettings()));

    public static final Item STEEL_TONG = register("steel_tong",
            new Item(new FabricItemSettings()));
    public static final Item IRON_TONG = register("iron_tong",
            new Item(new FabricItemSettings()));
    public static final Item WOODEN_TONGS = register("wooden_tongs",
            new Tongs(ToolMaterials.WOOD, -1, -2f, new FabricItemSettings().maxDamage(120)));
    public static final Item IRON_TONGS = register("iron_tongs",
            new Tongs(ToolMaterials.IRON, -1, -2f, new FabricItemSettings().maxDamage(512)));

    public static final Item STEEL_TONGS = register("steel_tongs",
            new Tongs(ModToolTiers.STEEL, -1, -2f, new FabricItemSettings().maxDamage(1024)));

    public static final Item STONE_HAMMER_HEAD = register("stone_hammer_head",
            new Item(new FabricItemSettings()));

    public static final Item COPPER_HAMMER_HEAD = register("copper_hammer_head",
            new Item(new FabricItemSettings()));

    public static final Item STEEL_HAMMER_HEAD = register("steel_hammer_head",
            new Item(new FabricItemSettings()));

    public static final Item SMITHING_HAMMER = register("smithing_hammer",
            new SmithingHammer(ModToolTiers.STEEL, -1, -2.8f, new FabricItemSettings()));

    public static final Item COPPER_SMITHING_HAMMER = register("copper_smithing_hammer",
            new SmithingHammer(ModToolTiers.COPPER, -1, -2.8f, new FabricItemSettings()));

    public static final Item DIAMOND_UPGRADE_SMITHING_TEMPLATE = register("diamond_upgrade_smithing_template",
            DiamondUpgradeTemplateItem.createDiamondUpgradeTemplate());

    public static final Item EMPTY_BLUEPRINT = register("empty_blueprint",
            new Item(new FabricItemSettings()));

    public static final Item BLUEPRINT = register("blueprint",
            new BlueprintItem(new FabricItemSettings()));

    public static final Item STONE_SWORD_BLADE = register("stone_sword_blade",
            new Item(new FabricItemSettings()));
    public static final Item IRON_SWORD_BLADE = register("iron_sword_blade",
            new Item(new FabricItemSettings()));
    public static final Item GOLDEN_SWORD_BLADE = register("golden_sword_blade",
            new Item(new FabricItemSettings()));
    public static final Item STEEL_SWORD_BLADE = register("steel_sword_blade",
            new Item(new FabricItemSettings()));
    public static final Item COPPER_SWORD_BLADE = register("copper_sword_blade",
            new Item(new FabricItemSettings()));

    public static final Item STONE_PICKAXE_HEAD = register("stone_pickaxe_head",
            new Item(new FabricItemSettings()));
    public static final Item IRON_PICKAXE_HEAD = register("iron_pickaxe_head",
            new Item(new FabricItemSettings()));
    public static final Item GOLDEN_PICKAXE_HEAD = register("golden_pickaxe_head",
            new Item(new FabricItemSettings()));
    public static final Item STEEL_PICKAXE_HEAD = register("steel_pickaxe_head",
            new Item(new FabricItemSettings()));
    public static final Item COPPER_PICKAXE_HEAD = register("copper_pickaxe_head",
            new Item(new FabricItemSettings()));


    public static final Item STONE_AXE_HEAD = register("stone_axe_head",
            new Item(new FabricItemSettings()));
    public static final Item IRON_AXE_HEAD = register("iron_axe_head",
            new Item(new FabricItemSettings()));
    public static final Item GOLDEN_AXE_HEAD = register("golden_axe_head",
            new Item(new FabricItemSettings()));
    public static final Item STEEL_AXE_HEAD = register("steel_axe_head",
            new Item(new FabricItemSettings()));
    public static final Item COPPER_AXE_HEAD = register("copper_axe_head",
            new Item(new FabricItemSettings()));


    public static final Item STONE_SHOVEL_HEAD = register("stone_shovel_head",
            new Item(new FabricItemSettings()));
    public static final Item IRON_SHOVEL_HEAD = register("iron_shovel_head",
            new Item(new FabricItemSettings()));
    public static final Item GOLDEN_SHOVEL_HEAD = register("golden_shovel_head",
            new Item(new FabricItemSettings()));
    public static final Item STEEL_SHOVEL_HEAD = register("steel_shovel_head",
            new Item(new FabricItemSettings()));
    public static final Item COPPER_SHOVEL_HEAD = register("copper_shovel_head",
            new Item(new FabricItemSettings()));


    public static final Item STONE_HOE_HEAD = register("stone_hoe_head",
            new Item(new FabricItemSettings()));
    public static final Item IRON_HOE_HEAD = register("iron_hoe_head",
            new Item(new FabricItemSettings()));
    public static final Item GOLDEN_HOE_HEAD = register("golden_hoe_head",
            new Item(new FabricItemSettings()));
    public static final Item STEEL_HOE_HEAD = register("steel_hoe_head",
            new Item(new FabricItemSettings()));
    public static final Item COPPER_HOE_HEAD = register("copper_hoe_head",
            new Item(new FabricItemSettings()));


    public static final Item STEEL_SWORD = register("steel_sword",
            new SwordItem(ModToolTiers.STEEL, 3, -2.4f, new FabricItemSettings()));
    public static final Item STEEL_PICKAXE = register("steel_pickaxe",
            new ModPickaxeItem(ModToolTiers.STEEL, 1, -2.8f, new FabricItemSettings()));
    public static final Item STEEL_AXE = register("steel_axe",
            new ModAxeItem(ModToolTiers.STEEL, 5, -3f, new FabricItemSettings()));
    public static final Item STEEL_HOE = register("steel_hoe",
            new ModHoeItem(ModToolTiers.STEEL, -3, -0.5f, new FabricItemSettings()));
    public static final Item STEEL_SHOVEL = register("steel_shovel",
            new ShovelItem(ModToolTiers.STEEL, 1, -3, new FabricItemSettings()));

    public static final Item STEEL_HELMET = register("steel_helmet",
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item STEEL_CHESTPLATE = register("steel_chestplate",
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
    public static final Item STEEL_LEGGINGS = register("steel_leggings",
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
    public static final Item STEEL_BOOTS = register("steel_boots",
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.BOOTS, new FabricItemSettings()));

    public static final Item COPPER_HELMET = register("copper_helmet",
            new CopperHelmet(ModArmorMaterials.COPPER, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item COPPER_CHESTPLATE = register("copper_chestplate",
            new ArmorItem(ModArmorMaterials.COPPER, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
    public static final Item COPPER_LEGGINGS = register("copper_leggings",
            new CopperLeggings(ModArmorMaterials.COPPER, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
    public static final Item COPPER_BOOTS = register("copper_boots",
            new ArmorItem(ModArmorMaterials.COPPER, ArmorItem.Type.BOOTS, new FabricItemSettings()));

    public static final Item COPPER_SWORD = register("copper_sword",
            new SwordItem(ModToolTiers.COPPER, 3, -2.4f, new FabricItemSettings()));
    public static final Item COPPER_PICKAXE = register("copper_pickaxe",
            new ModPickaxeItem(ModToolTiers.COPPER, 1, -2.8f, new FabricItemSettings()));
    public static final Item COPPER_AXE = register("copper_axe",
            new ModAxeItem(ModToolTiers.COPPER, 5, -3f, new FabricItemSettings()));
    public static final Item COPPER_HOE = register("copper_hoe",
            new ModHoeItem(ModToolTiers.COPPER, -1, -1.5f, new FabricItemSettings()));
    public static final Item COPPER_SHOVEL = register("copper_shovel",
            new ShovelItem(ModToolTiers.COPPER, 1.5f, -3, new FabricItemSettings()));

    public static final Item LINGERING_ARROW = register("lingering_arrow",
            new LingeringArrowItem(new FabricItemSettings(), ArrowTier.FLINT));

    public static final Item IRON_UPGRADE_ARROW = register("iron_arrow",
            new UpgradeArrowItem(new FabricItemSettings(), ArrowTier.IRON));
    public static final Item STEEL_UPGRADE_ARROW = register("steel_arrow",
            new UpgradeArrowItem(new FabricItemSettings(), ArrowTier.STEEL));
    public static final Item DIAMOND_UPGRADE_ARROW = register("diamond_arrow",
            new UpgradeArrowItem(new FabricItemSettings(), ArrowTier.DIAMOND));


    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Overgeared.id(name), item);
    }

    public static void register() {
    }
}
