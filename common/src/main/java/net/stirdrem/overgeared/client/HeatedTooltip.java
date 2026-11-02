package net.stirdrem.overgeared.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

/**
 * Client-only tooltip line showing how long until a heated item cools off. Uses the same
 * HeatedSince timestamp + HEATED_ITEM_COOLDOWN_TICKS the server cools items with
 * (ItemStackMixin#onInventoryTick, AbstractSmithingAnvilBlockEntity#tickHeatedIngredients).
 * Tooltips are rebuilt every frame, so the countdown updates live.
 */
public final class HeatedTooltip {
    private static final String HEATED_TIME_TAG = "HeatedSince";
    private static final int WARNING_TICKS = 200; // last 10 seconds

    private HeatedTooltip() {
    }

    /** The "Cools in m:ss" line, or null if the stack isn't heated or hasn't been stamped yet. */
    @Nullable
    public static Component getCooldownLine(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(HEATED_TIME_TAG)) return null;
        if (!stack.is(ModTags.Items.HEATED_METALS) && !tag.contains("Heated")) return null;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;

        long heatedSince = tag.getLong(HEATED_TIME_TAG);
        if (heatedSince == 0L) return null;

        long elapsed = mc.level.getGameTime() - heatedSince;
        int remaining = (int) Math.max(0L, Overgeared.getConfig().HEATED_ITEM_COOLDOWN_TICKS() - elapsed);

        ChatFormatting color = remaining <= WARNING_TICKS ? ChatFormatting.YELLOW : ChatFormatting.GOLD;
        return Component.translatable("tooltip.overgeared.heated_timer", StringUtil.formatTickDuration(remaining))
                .withStyle(color);
    }
}
