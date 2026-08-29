package net.stirdrem.overgeared.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.BreakSystemBlacklistReloadListener;

public class BrokenHelper {
    public static boolean isBroken(ItemStack stack) {
        if (!ServerConfig.ENABLE_QUALITY_BREAK_SYSTEM.get()) {
            return false;
        }
        if (isBlacklisted(stack)) return false;
        return stack.isDamageable() && stack.getMaxDamage() > 0 && stack.getDamage() >= stack.getMaxDamage();
    }

    private static boolean isBlacklisted(ItemStack stack) {
        Item item = stack.getItem();
        Identifier itemId = Registries.ITEM.getId(item);

        for (String entry : ServerConfig.QUALITY_BREAK_BLACKLIST.get()) {
            if (entry.startsWith("#")) {
                TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, Identifier.tryParse(entry.substring(1)));
                if (stack.isIn(tag)) return true;
            } else if (itemId != null && itemId.equals(Identifier.tryParse(entry))) {
                return true;
            }
        }

        return BreakSystemBlacklistReloadListener.isBlacklisted(stack);
    }
}
