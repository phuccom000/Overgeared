package net.stirdrem.overgeared.item.custom;

import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class DiamondUpgradeTemplateItem {
    private static final Text DIAMOND_UPGRADE = Text.translatable("upgrade.overgeared.diamond_upgrade").formatted(Formatting.GRAY);
    private static final Text DIAMOND_UPGRADE_APPLIES_TO = Text.translatable("item.overgeared.smithing_template.diamond_upgrade.applies_to").formatted(Formatting.BLUE);
    private static final Text DIAMOND_UPGRADE_INGREDIENTS = Text.translatable("item.overgeared.smithing_template.diamond_upgrade.ingredients").formatted(Formatting.BLUE);
    private static final Text DIAMOND_UPGRADE_BASE_SLOT_DESCRIPTION = Text.translatable("item.overgeared.smithing_template.diamond_upgrade.base_slot_description");
    private static final Text DIAMOND_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Text.translatable("item.overgeared.smithing_template.diamond_upgrade.additions_slot_description");
    private static final Identifier EMPTY_SLOT_HELMET = Identifier.tryParse("item/empty_armor_slot_helmet");
    private static final Identifier EMPTY_SLOT_CHESTPLATE = Identifier.tryParse("item/empty_armor_slot_chestplate");
    private static final Identifier EMPTY_SLOT_LEGGINGS = Identifier.tryParse("item/empty_armor_slot_leggings");
    private static final Identifier EMPTY_SLOT_BOOTS = Identifier.tryParse("item/empty_armor_slot_boots");
    private static final Identifier EMPTY_SLOT_HOE = Identifier.tryParse("item/empty_slot_hoe");
    private static final Identifier EMPTY_SLOT_AXE = Identifier.tryParse("item/empty_slot_axe");
    private static final Identifier EMPTY_SLOT_SWORD = Identifier.tryParse("item/empty_slot_sword");
    private static final Identifier EMPTY_SLOT_SHOVEL = Identifier.tryParse("item/empty_slot_shovel");
    private static final Identifier EMPTY_SLOT_PICKAXE = Identifier.tryParse("item/empty_slot_pickaxe");
    private static final Identifier EMPTY_SLOT_DIAMOND = Identifier.tryParse("item/empty_slot_diamond");

    public static SmithingTemplateItem createDiamondUpgradeTemplate() {
        return new SmithingTemplateItem(DIAMOND_UPGRADE_APPLIES_TO, DIAMOND_UPGRADE_INGREDIENTS, DIAMOND_UPGRADE,
                DIAMOND_UPGRADE_BASE_SLOT_DESCRIPTION, DIAMOND_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
                createDiamondUpgradeIconList(), createDiamondUpgradeMaterialList());
    }

    private static List<Identifier> createDiamondUpgradeIconList() {
        return List.of(EMPTY_SLOT_HELMET, EMPTY_SLOT_SWORD, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_AXE, EMPTY_SLOT_BOOTS, EMPTY_SLOT_HOE, EMPTY_SLOT_SHOVEL);
    }

    private static List<Identifier> createDiamondUpgradeMaterialList() {
        return List.of(EMPTY_SLOT_DIAMOND);
    }
}
