package net.stirdrem.overgeared.util;

import net.minecraft.world.item.ItemStack;

public class BrokenHelper {
    public static boolean isBroken(ItemStack stack) {
        return stack.isDamageableItem() && stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage();
    }
}
