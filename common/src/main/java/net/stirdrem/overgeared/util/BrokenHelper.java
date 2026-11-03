package net.stirdrem.overgeared.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.BreakSystemBlacklistReloadListener;
import net.stirdrem.overgeared.datapack.DurabilityBlacklistReloadListener;

import java.util.List;

public class BrokenHelper {
    public static boolean isBroken(ItemStack stack) {
        if (!ServerConfig.ENABLE_QUALITY_BREAK_SYSTEM.get()) {
            return false;
        }
        if (isBlacklisted(stack)) return false;
        return stack.isDamageableItem() && stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage();
    }

    public static boolean isBlacklisted(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<? extends String> blacklist = ServerConfig.BASE_DURABILITY_BLACKLIST.get();

        for (String entry : blacklist) {
            if (entry.startsWith("#")) {
                // Handle tag entries like "#forge:tools/hammers"
                ResourceLocation tagId = ResourceLocation.tryParse(entry.substring(1));
                TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                if (stack.is(tag)) return true;
            } else {
                // Handle direct item IDs
                if (itemId != null && itemId.equals(ResourceLocation.tryParse(entry))) return true;
            }
        }
        return BreakSystemBlacklistReloadListener.isBlacklisted(stack);
    }
}
