package net.stirdrem.overgeared.util;

import net.stirdrem.overgeared.Overgeared;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.datapack.BreakSystemBlacklistReloadListener;

public class BrokenHelper {
    public static boolean isBroken(ItemStack stack) {
        if (!Overgeared.getConfig().ENABLE_QUALITY_BREAK_SYSTEM()) {
            return false;
        }
        if (isBlacklisted(stack)) return false;
        return stack.isDamageableItem() && stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage();
    }

    private static boolean isBlacklisted(ItemStack stack) {
        Item item = stack.getItem();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        for (String entry : Overgeared.getConfig().QUALITY_BREAK_BLACKLIST()) {
            if (entry.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.tryParse(entry.substring(1)));
                if (stack.is(tag)) return true;
            } else if (itemId != null && itemId.equals(ResourceLocation.tryParse(entry))) {
                return true;
            }
        }

        return BreakSystemBlacklistReloadListener.isBlacklisted(stack);
    }
}
