package net.stirdrem.overgeared.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Item classification. Since 1.21.2 tools, weapons and armor are plain items configured through
 * data components, so the old {@code instanceof ArmorItem / TieredItem / SwordItem} checks are
 * answered from those components (and the vanilla item tags) instead.
 */
public final class ItemKinds {
    private ItemKinds() {
    }

    /** Worn armor: equippable in a humanoid armor slot (what {@code ArmorItem} used to be). */
    public static boolean isArmor(Item item) {
        Equippable equippable = item.components().get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    public static boolean isArmor(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    /** Tools and melee weapons with a material tier (what {@code TieredItem} used to be). */
    public static boolean isTiered(Item item) {
        return item.components().has(DataComponents.TOOL) || item.components().has(DataComponents.WEAPON);
    }

    public static boolean isTiered(ItemStack stack) {
        return stack.has(DataComponents.TOOL) || stack.has(DataComponents.WEAPON);
    }

    public static boolean isSword(ItemStack stack) {
        return stack.is(ItemTags.SWORDS);
    }

    public static boolean isShield(ItemStack stack) {
        return stack.has(DataComponents.BLOCKS_ATTACKS);
    }
}
