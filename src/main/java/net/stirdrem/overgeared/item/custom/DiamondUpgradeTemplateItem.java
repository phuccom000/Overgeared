package net.stirdrem.overgeared.item.custom;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;

public class DiamondUpgradeTemplateItem {
    private static final Component DIAMOND_UPGRADE = Component.translatable("upgrade.overgeared.diamond_upgrade").withStyle(ChatFormatting.GRAY);
    private static final Component DIAMOND_UPGRADE_APPLIES_TO = Component.translatable("item.overgeared.smithing_template.diamond_upgrade.applies_to").withStyle(ChatFormatting.BLUE);
    private static final Component DIAMOND_UPGRADE_INGREDIENTS = Component.translatable("item.overgeared.smithing_template.diamond_upgrade.ingredients").withStyle(ChatFormatting.BLUE);
    private static final Component DIAMOND_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable("item.overgeared.smithing_template.diamond_upgrade.base_slot_description");
    private static final Component DIAMOND_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable("item.overgeared.smithing_template.diamond_upgrade.additions_slot_description");
    private static final Identifier EMPTY_SLOT_HELMET = Identifier.withDefaultNamespace("container/slot/helmet");
    private static final Identifier EMPTY_SLOT_CHESTPLATE = Identifier.withDefaultNamespace("container/slot/chestplate");
    private static final Identifier EMPTY_SLOT_LEGGINGS = Identifier.withDefaultNamespace("container/slot/leggings");
    private static final Identifier EMPTY_SLOT_BOOTS = Identifier.withDefaultNamespace("container/slot/boots");
    private static final Identifier EMPTY_SLOT_HOE = Identifier.withDefaultNamespace("container/slot/hoe");
    private static final Identifier EMPTY_SLOT_AXE = Identifier.withDefaultNamespace("container/slot/axe");
    private static final Identifier EMPTY_SLOT_SWORD = Identifier.withDefaultNamespace("container/slot/sword");
    private static final Identifier EMPTY_SLOT_SPEAR = Identifier.withDefaultNamespace("container/slot/spear");
    private static final Identifier EMPTY_SLOT_SHOVEL = Identifier.withDefaultNamespace("container/slot/shovel");
    private static final Identifier EMPTY_SLOT_PICKAXE = Identifier.withDefaultNamespace("container/slot/pickaxe");
    private static final Identifier EMPTY_SLOT_DIAMOND = Identifier.withDefaultNamespace("container/slot/diamond");

    public static SmithingTemplateItem createDiamondUpgradeTemplate(Item.Properties properties) {
        return new SmithingTemplateItem(DIAMOND_UPGRADE_APPLIES_TO, DIAMOND_UPGRADE_INGREDIENTS,
                DIAMOND_UPGRADE_BASE_SLOT_DESCRIPTION, DIAMOND_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
                createDiamondUpgradeIconList(), createDiamondUpgradeMaterialList(), properties);
    }

    private static List<Identifier> createDiamondUpgradeIconList() {
        return List.of(EMPTY_SLOT_HELMET, EMPTY_SLOT_SWORD, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_AXE, EMPTY_SLOT_BOOTS, EMPTY_SLOT_HOE, EMPTY_SLOT_SHOVEL, EMPTY_SLOT_SPEAR);
    }

    private static List<Identifier> createDiamondUpgradeMaterialList() {
        return List.of(EMPTY_SLOT_DIAMOND);
    }
}
