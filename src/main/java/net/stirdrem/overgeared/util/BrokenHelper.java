package net.stirdrem.overgeared.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.BreakSystemBlacklistReloadListener;

public class BrokenHelper {
    public static boolean isBroken(ItemStack stack) {
        if (!ServerConfig.ENABLE_QUALITY_BREAK_SYSTEM.get()) {
            return false;
        }
        if (isBlacklisted(stack)) return false;
        return stack.isDamageableItem() && stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage();
    }

    private static boolean isBlacklisted(ItemStack stack) {
        Item item = stack.getItem();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        for (String entry : ServerConfig.QUALITY_BREAK_BLACKLIST.get()) {
            if (entry.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(entry.substring(1)));
                if (stack.is(tag)) return true;
            } else if (itemId != null && itemId.equals(ResourceLocation.parse(entry))) {
                return true;
            }
        }

        return BreakSystemBlacklistReloadListener.isBlacklisted(stack);
    }
}
