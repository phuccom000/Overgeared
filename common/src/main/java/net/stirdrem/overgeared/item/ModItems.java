package net.stirdrem.overgeared.item;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.stirdrem.overgeared.registry.DeferredItem;
import net.stirdrem.overgeared.registry.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.item.custom.*;
import net.minecraft.world.item.equipment.ArmorType;
import net.stirdrem.overgeared.util.ModTags;

import java.util.function.Function;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(OvergearedMod.MOD_ID);

    public static final DeferredItem<Item> CRUDE_STEEL = registerSimpleItem("crude_steel");
    public static final DeferredItem<Item> HEATED_CRUDE_STEEL = registerSimpleItem("heated_crude_steel");

    public static final DeferredItem<Item> ROCK = registerSimpleItem("knappable_rock");

    public static final DeferredItem<Item> UNFIRED_TOOL_CAST = registerItem("unfired_tool_cast",
            p -> new ClayToolCastItem(p));

    public static final DeferredItem<Item> CLAY_TOOL_CAST = registerItem("clay_tool_cast",
            p -> new ToolCastItem(true, p.durability(1)));

    public static final DeferredItem<Item> NETHER_TOOL_CAST = registerItem("nether_tool_cast",
            p -> new ToolCastItem(false, p.stacksTo(1)));

    public static final DeferredItem<Item> STEEL_NUGGET = registerSimpleItem("steel_nugget");

    public static final DeferredItem<Item> STEEL_INGOT = registerSimpleItem("steel_ingot");

    public static final DeferredItem<Item> IRON_ARROW_HEAD = registerSimpleItem("iron_arrow_head");
    public static final DeferredItem<Item> STEEL_ARROW_HEAD = registerSimpleItem("steel_arrow_head");

    public static final DeferredItem<Item> DIAMOND_SHARD = registerSimpleItem("diamond_shard");

    public static final DeferredItem<Item> NETHERITE_ALLOY = registerSimpleItem("netherite_alloy");

    public static final DeferredItem<Item> HEATED_IRON_INGOT = registerSimpleItem("heated_iron_ingot");
    public static final DeferredItem<Item> HEATED_COPPER_INGOT = registerSimpleItem("heated_copper_ingot");
    public static final DeferredItem<Item> HEATED_STEEL_INGOT = registerSimpleItem("heated_steel_ingot");
    public static final DeferredItem<Item> HEATED_SILVER_INGOT = registerSimpleItem("heated_silver_ingot");
    public static final DeferredItem<Item> HEATED_NETHERITE_ALLOY = registerSimpleItem("heated_netherite_alloy");

    public static final DeferredItem<Item> COPPER_PLATE = registerSimpleItem("copper_plate");
    public static final DeferredItem<Item> IRON_PLATE = registerSimpleItem("iron_plate");
    public static final DeferredItem<Item> STEEL_PLATE = registerSimpleItem("steel_plate");

    public static final DeferredItem<Item> STEEL_TONG = registerSimpleItem("steel_tong");
    public static final DeferredItem<Item> IRON_TONG = registerSimpleItem("iron_tong");

    public static final DeferredItem<Item> WOODEN_TONGS = registerItem("wooden_tongs",
            p -> new Tongs(p.tool(ToolMaterial.WOOD, ModTags.Blocks.SMITHING, -1, -2f, 0).durability(120)));
    public static final DeferredItem<Item> IRON_TONGS = registerItem("iron_tongs",
            p -> new Tongs(p.tool(ToolMaterial.IRON, ModTags.Blocks.SMITHING, -1, -2f, 0).durability(512)));
    public static final DeferredItem<Item> STEEL_TONGS = registerItem("steel_tongs",
            p -> new Tongs(p.tool(ModToolTiers.STEEL, ModTags.Blocks.SMITHING, -1, -2f, 0).durability(1024)));

    public static final DeferredItem<Item> STONE_HAMMER_HEAD = registerSimpleItem("stone_hammer_head");
    public static final DeferredItem<Item> COPPER_HAMMER_HEAD = registerSimpleItem("copper_hammer_head");
    public static final DeferredItem<Item> STEEL_HAMMER_HEAD = registerSimpleItem("steel_hammer_head");

    public static final DeferredItem<Item> SMITHING_HAMMER = registerItem("smithing_hammer",
            p -> new SmithingHammer(toolProperties(p.tool(ModToolTiers.STEEL, ModTags.Blocks.SMITHING, 0, 0, 0), -1, -2.8f)));

    public static final DeferredItem<Item> COPPER_SMITHING_HAMMER = registerItem("copper_smithing_hammer",
            p -> new SmithingHammer(toolProperties(p.tool(ToolMaterial.COPPER, ModTags.Blocks.SMITHING, 0, 0, 0), -1, -2.8f)));

    public static final DeferredItem<Item> DIAMOND_UPGRADE_SMITHING_TEMPLATE = registerItem("diamond_upgrade_smithing_template",
            DiamondUpgradeTemplateItem::createDiamondUpgradeTemplate);

    public static final DeferredItem<Item> EMPTY_BLUEPRINT = registerSimpleItem("empty_blueprint");

    public static final DeferredItem<Item> BLUEPRINT = registerItem("blueprint",
            p -> new BlueprintItem(p));

    public static final DeferredItem<Item> STONE_SWORD_BLADE = registerSimpleItem("stone_sword_blade");
    public static final DeferredItem<Item> IRON_SWORD_BLADE = registerSimpleItem("iron_sword_blade");
    public static final DeferredItem<Item> GOLDEN_SWORD_BLADE = registerSimpleItem("golden_sword_blade");
    public static final DeferredItem<Item> STEEL_SWORD_BLADE = registerSimpleItem("steel_sword_blade");
    public static final DeferredItem<Item> COPPER_SWORD_BLADE = registerSimpleItem("copper_sword_blade");

    public static final DeferredItem<Item> STONE_PICKAXE_HEAD = registerSimpleItem("stone_pickaxe_head");
    public static final DeferredItem<Item> IRON_PICKAXE_HEAD = registerSimpleItem("iron_pickaxe_head");
    public static final DeferredItem<Item> GOLDEN_PICKAXE_HEAD = registerSimpleItem("golden_pickaxe_head");
    public static final DeferredItem<Item> STEEL_PICKAXE_HEAD = registerSimpleItem("steel_pickaxe_head");
    public static final DeferredItem<Item> COPPER_PICKAXE_HEAD = registerSimpleItem("copper_pickaxe_head");

    public static final DeferredItem<Item> STONE_AXE_HEAD = registerSimpleItem("stone_axe_head");
    public static final DeferredItem<Item> IRON_AXE_HEAD = registerSimpleItem("iron_axe_head");
    public static final DeferredItem<Item> GOLDEN_AXE_HEAD = registerSimpleItem("golden_axe_head");
    public static final DeferredItem<Item> STEEL_AXE_HEAD = registerSimpleItem("steel_axe_head");
    public static final DeferredItem<Item> COPPER_AXE_HEAD = registerSimpleItem("copper_axe_head");

    public static final DeferredItem<Item> STONE_SHOVEL_HEAD = registerSimpleItem("stone_shovel_head");
    public static final DeferredItem<Item> IRON_SHOVEL_HEAD = registerSimpleItem("iron_shovel_head");
    public static final DeferredItem<Item> GOLDEN_SHOVEL_HEAD = registerSimpleItem("golden_shovel_head");
    public static final DeferredItem<Item> STEEL_SHOVEL_HEAD = registerSimpleItem("steel_shovel_head");
    public static final DeferredItem<Item> COPPER_SHOVEL_HEAD = registerSimpleItem("copper_shovel_head");

    public static final DeferredItem<Item> STONE_HOE_HEAD = registerSimpleItem("stone_hoe_head");
    public static final DeferredItem<Item> IRON_HOE_HEAD = registerSimpleItem("iron_hoe_head");
    public static final DeferredItem<Item> GOLDEN_HOE_HEAD = registerSimpleItem("golden_hoe_head");
    public static final DeferredItem<Item> STEEL_HOE_HEAD = registerSimpleItem("steel_hoe_head");
    public static final DeferredItem<Item> COPPER_HOE_HEAD = registerSimpleItem("copper_hoe_head");

    public static final DeferredItem<Item> STONE_SPEAR_HEAD = registerSimpleItem("stone_spear_head");
    public static final DeferredItem<Item> IRON_SPEAR_HEAD = registerSimpleItem("iron_spear_head");
    public static final DeferredItem<Item> GOLDEN_SPEAR_HEAD = registerSimpleItem("golden_spear_head");
    public static final DeferredItem<Item> STEEL_SPEAR_HEAD = registerSimpleItem("steel_spear_head");
    public static final DeferredItem<Item> COPPER_SPEAR_HEAD = registerSimpleItem("copper_spear_head");

    public static final DeferredItem<Item> STEEL_SWORD = registerItem("steel_sword",
            p -> new Item(toolProperties(p.sword(ModToolTiers.STEEL, 0, 0), 6, -2.4f)));
    public static final DeferredItem<Item> STEEL_PICKAXE = registerItem("steel_pickaxe",
            p -> new Item(toolProperties(p.pickaxe(ModToolTiers.STEEL, 0, 0), 4, -2.8f)));
    public static final DeferredItem<Item> STEEL_AXE = registerItem("steel_axe",
            p -> new Item(toolProperties(p.axe(ModToolTiers.STEEL, 0, 0), 8, -3f)));
    public static final DeferredItem<Item> STEEL_HOE = registerItem("steel_hoe",
            p -> new Item(toolProperties(p.hoe(ModToolTiers.STEEL, 0, 0), 0, -0.5f)));
    public static final DeferredItem<Item> STEEL_SHOVEL = registerItem("steel_shovel",
            p -> new Item(toolProperties(p.shovel(ModToolTiers.STEEL, 0, 0), 4, -3)));
    // 26.x spear: attack duration, damage multiplier and charge timings sit between vanilla's
    // iron and diamond spears, like the rest of the steel tier
    public static final DeferredItem<Item> STEEL_SPEAR = registerItem("steel_spear",
            p -> new Item(p.spear(ModToolTiers.STEEL, 1.0F, 1.0125F, 0.55F, 2.75F, 10.5F, 6.625F, 5.1F, 10.625F, 4.6F)));

    public static final DeferredItem<Item> STEEL_HELMET = registerItem("steel_helmet",
            p -> new Item(p.humanoidArmor(ModArmorMaterials.STEEL, ArmorType.HELMET)));
    public static final DeferredItem<Item> STEEL_CHESTPLATE = registerItem("steel_chestplate",
            p -> new Item(p.humanoidArmor(ModArmorMaterials.STEEL, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> STEEL_LEGGINGS = registerItem("steel_leggings",
            p -> new Item(p.humanoidArmor(ModArmorMaterials.STEEL, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> STEEL_BOOTS = registerItem("steel_boots",
            p -> new Item(p.humanoidArmor(ModArmorMaterials.STEEL, ArmorType.BOOTS)));

    public static final DeferredItem<Item> LINGERING_ARROW = registerItem("lingering_arrow",
            p -> new LingeringArrowItem(p, ArrowTier.FLINT));

    public static final DeferredItem<Item> IRON_UPGRADE_ARROW = registerItem("iron_arrow",
            p -> new UpgradeArrowItem(p, ArrowTier.IRON));
    public static final DeferredItem<Item> STEEL_UPGRADE_ARROW = registerItem("steel_arrow",
            p -> new UpgradeArrowItem(p, ArrowTier.STEEL));
    public static final DeferredItem<Item> DIAMOND_UPGRADE_ARROW = registerItem("diamond_arrow",
            p -> new UpgradeArrowItem(p, ArrowTier.DIAMOND));

    /** Replaces the attribute modifiers set by the tool properties with these exact values (as in 1.21.1). */
    private static Item.Properties toolProperties(Item.Properties properties, float attackDamage, float attackSpeed) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_ID,
                                attackDamage,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                Item.BASE_ATTACK_SPEED_ID,
                                attackSpeed,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                );
        return properties.attributes(builder.build());
    }

    public static <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, T> factory) {
        return ITEMS.registerItem(name, factory);
    }

    public static DeferredItem<Item> registerSimpleItem(String name) {
        return ITEMS.registerSimpleItem(name);
    }

    public static void register() {
        ITEMS.register();
    }
}
