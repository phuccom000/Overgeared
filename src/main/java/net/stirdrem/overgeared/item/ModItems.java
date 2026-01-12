package net.stirdrem.overgeared.item;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.item.custom.*;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(OvergearedMod.MOD_ID);

    public static final DeferredItem<Item> CRUDE_STEEL = registerSimpleItem("crude_steel");
    public static final DeferredItem<Item> HEATED_CRUDE_STEEL = registerSimpleItem("heated_crude_steel");

    public static final DeferredItem<Item> ROCK = registerSimpleItem("knappable_rock");

    public static final DeferredItem<Item> UNFIRED_TOOL_CAST = registerItem("unfired_tool_cast",
            () -> new ToolCastItem(false, false, new Item.Properties()));

    public static final DeferredItem<Item> CLAY_TOOL_CAST = registerItem("clay_tool_cast",
            () -> new ToolCastItem(true, true,
                    new Item.Properties().stacksTo(1).durability(1)));

    public static final DeferredItem<Item> NETHER_TOOL_CAST = registerItem("nether_tool_cast",
            () -> new ToolCastItem(true, false,
                    new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> STEEL_NUGGET = registerSimpleItem("steel_nugget");
    public static final DeferredItem<Item> COPPER_NUGGET = registerSimpleItem("copper_nugget");

    public static final DeferredItem<Item> STEEL_INGOT = registerSimpleItem("steel_ingot");

    public static final DeferredItem<Item> IRON_ARROW_HEAD =registerSimpleItem("iron_arrow_head");
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
            () -> new Tongs(Tiers.WOOD, -1, -2f,
                    new Item.Properties().durability(120)));
    public static final DeferredItem<Item> IRON_TONGS = registerItem("iron_tongs",
            () -> new Tongs(Tiers.IRON, -1, -2f,
                    new Item.Properties().durability(512)));
    public static final DeferredItem<Item> STEEL_TONGS = registerItem("steel_tongs",
            () -> new Tongs(ModToolTiers.STEEL, -1, -2f,
                    new Item.Properties().durability(1024)));

    public static final DeferredItem<Item> STONE_HAMMER_HEAD = registerSimpleItem("stone_hammer_head");
    public static final DeferredItem<Item> COPPER_HAMMER_HEAD = registerSimpleItem("copper_hammer_head");
    public static final DeferredItem<Item> STEEL_HAMMER_HEAD = registerSimpleItem("steel_hammer_head");

    public static final DeferredItem<Item> SMITHING_HAMMER = registerItem("smithing_hammer",
            () -> new SmithingHammer(ModToolTiers.STEEL, toolProperties(-1, -2.8f).durability(512)));

    public static final DeferredItem<Item> COPPER_SMITHING_HAMMER = registerItem("copper_smithing_hammer",
            () -> new SmithingHammer(ModToolTiers.COPPER, toolProperties(-1, -2.8f).durability(120)));

    public static final DeferredItem<Item> DIAMOND_UPGRADE_SMITHING_TEMPLATE = registerItem("diamond_upgrade_smithing_template",
            DiamondUpgradeTemplateItem::createDiamondUpgradeTemplate);

    public static final DeferredItem<Item> EMPTY_BLUEPRINT = registerSimpleItem("empty_blueprint");

    public static final DeferredItem<Item> BLUEPRINT = registerItem("blueprint",
            () -> new BlueprintItem(new Item.Properties()));

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
    
    public static final DeferredItem<Item> STEEL_SWORD = registerItem("steel_sword",
            () -> new SwordItem(ModToolTiers.STEEL, toolProperties(6, -2.4f)));
    public static final DeferredItem<Item> STEEL_PICKAXE = registerItem("steel_pickaxe",
            () -> new PickaxeItem(ModToolTiers.STEEL, toolProperties(4, -2.8f)));
    public static final DeferredItem<Item> STEEL_AXE = registerItem("steel_axe",
            () -> new AxeItem(ModToolTiers.STEEL, toolProperties(8, -3f)));
    public static final DeferredItem<Item> STEEL_HOE = registerItem("steel_hoe",
            () -> new HoeItem(ModToolTiers.STEEL, toolProperties(0, -0.5f)));
    public static final DeferredItem<Item> STEEL_SHOVEL = registerItem("steel_shovel",
            () -> new ShovelItem(ModToolTiers.STEEL, toolProperties(4, -3)));
    
    public static final DeferredItem<Item> STEEL_HELMET = registerItem("steel_helmet",
            () -> new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> STEEL_CHESTPLATE = registerItem("steel_chestplate",
            () -> new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> STEEL_LEGGINGS = registerItem("steel_leggings",
            () -> new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<Item> STEEL_BOOTS = registerItem("steel_boots",
            () -> new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<Item> COPPER_HELMET = registerItem("copper_helmet",
            () -> new ArmorItem(ModArmorMaterials.COPPER, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> COPPER_CHESTPLATE = registerItem("copper_chestplate",
            () -> new ArmorItem(ModArmorMaterials.COPPER, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> COPPER_LEGGINGS = registerItem("copper_leggings",
            () -> new ArmorItem(ModArmorMaterials.COPPER, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<Item> COPPER_BOOTS = registerItem("copper_boots",
            () -> new ArmorItem(ModArmorMaterials.COPPER, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<Item> COPPER_SWORD = registerItem("copper_sword",
            () -> new SwordItem(ModToolTiers.COPPER, toolProperties(4, -2.4f)));
    public static final DeferredItem<Item> COPPER_PICKAXE = registerItem("copper_pickaxe",
            () -> new PickaxeItem(ModToolTiers.COPPER, toolProperties(2, -2.8f)));
    public static final DeferredItem<Item> COPPER_AXE = registerItem("copper_axe",
            () -> new AxeItem(ModToolTiers.COPPER, toolProperties(6, -3f)));
    public static final DeferredItem<Item> COPPER_HOE = registerItem("copper_hoe",
            () -> new HoeItem(ModToolTiers.COPPER, toolProperties(0, -1.5f)));
    public static final DeferredItem<Item> COPPER_SHOVEL = registerItem("copper_shovel",
            () -> new ShovelItem(ModToolTiers.COPPER, toolProperties(2.5f, -3)));

    public static final DeferredItem<Item> LINGERING_ARROW = registerItem("lingering_arrow",
            () -> new LingeringArrowItem(new Item.Properties(), ArrowTier.FLINT));

    public static final DeferredItem<Item> IRON_UPGRADE_ARROW = registerItem("iron_arrow",
            () -> new UpgradeArrowItem(new Item.Properties(), ArrowTier.IRON));
    public static final DeferredItem<Item> STEEL_UPGRADE_ARROW = registerItem("steel_arrow",
            () -> new UpgradeArrowItem(new Item.Properties(), ArrowTier.STEEL));
    public static final DeferredItem<Item> DIAMOND_UPGRADE_ARROW = registerItem("diamond_arrow",
            () -> new UpgradeArrowItem(new Item.Properties(), ArrowTier.DIAMOND));

    private static Item.Properties toolProperties(float attackDamage, float attackSpeed) {
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
        return new Item.Properties().attributes(builder.build());
    }

    public static <T extends Item> DeferredItem<T> registerItem(String name, Supplier<T> itemSupplier) {
        return ITEMS.register(name, itemSupplier);
    }

    public static DeferredItem<Item> registerSimpleItem(String name) {
        return ITEMS.registerSimpleItem(name);
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
