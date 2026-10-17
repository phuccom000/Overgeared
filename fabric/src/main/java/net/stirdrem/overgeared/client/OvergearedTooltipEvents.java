package net.stirdrem.overgeared.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

/**
 * Ported from ModEvents.onTooltip (Forge's ItemTooltipEvent) - client-only, since it relies on
 * Fabric's client-side ItemTooltipCallback and Screen.hasShiftDown().
 */
public class OvergearedTooltipEvents {

    public static void register() {
        ItemTooltipCallback.EVENT.register(OvergearedTooltipEvents::onTooltip);
    }

    private static void onTooltip(ItemStack stack, net.minecraft.world.item.TooltipFlag context, List<Component> tooltip) {
        int insertOffset = 1;

        // Add Forging Quality
        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            String quality = stack.getTag().getString("ForgingQuality");
            Component qualityComponent = switch (quality) {
                case "poor" -> Component.translatable("tooltip.overgeared.poor").withStyle(ChatFormatting.RED);
                case "well" -> Component.translatable("tooltip.overgeared.well").withStyle(ChatFormatting.YELLOW);
                case "expert" -> Component.translatable("tooltip.overgeared.expert").withStyle(ChatFormatting.BLUE);
                case "perfect" -> Component.translatable("tooltip.overgeared.perfect").withStyle(ChatFormatting.GOLD);
                case "master" -> Component.translatable("tooltip.overgeared.master").withStyle(ChatFormatting.LIGHT_PURPLE);
                default -> null;
            };
            if (qualityComponent != null) {
                tooltip.add(insertOffset++, qualityComponent);
            }
        }
        if (isBroken(stack) && ServerConfig.ENABLE_MOD_TOOLTIPS.get()) {
            tooltip.add(insertOffset++,
                    Component.translatable("tooltip.overgeared.item_broken").withStyle(ChatFormatting.RED)
            );
        }
        if (stack.hasTag() && stack.getTag().contains("Heated")) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.heated").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }
        // Add Polish status
        if (stack.hasTag() && stack.getTag().contains("Polished")) {
            boolean isPolished = stack.getTag().getBoolean("Polished");
            Component polishComponent = isPolished
                    ? Component.translatable("tooltip.overgeared.polished").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)
                    : Component.translatable("tooltip.overgeared.unpolished").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC);
            tooltip.add(insertOffset++, polishComponent);
        }
        if (stack.hasTag() && stack.getTag().contains("failedResult")) {
            tooltip.add(insertOffset, Component.translatable("tooltip.overgeared.failedResult").withStyle(ChatFormatting.RED));
        }

        // Smithing Hammer special tooltip
        if (stack.is(ModTags.Items.SMITHING_HAMMERS)) {
            if (!Screen.hasShiftDown()) {
                tooltip.add(insertOffset, Component.translatable("tooltip.overgeared.smithing_hammer.hold_shift")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            } else {
                tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line1")
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line2")
                        .withStyle(ChatFormatting.GRAY));
                if (ServerConfig.ENABLE_STONE_TO_ANVIL.get())
                    tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line3")
                            .withStyle(ChatFormatting.GRAY));
                if (ServerConfig.ENABLE_ANVIL_TO_SMITHING.get())
                    tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line4")
                            .withStyle(ChatFormatting.GRAY));
            }
        }

        if (stack.is(Items.POTION) && ServerConfig.TIPPING_TOGGLE.get()) {
            CompoundTag tag = stack.getTag();
            int maxUses = ServerConfig.MAX_POTION_TIPPING_USE.get();
            int used = 0;

            if (tag != null && tag.contains("TippedUsed", Tag.TAG_INT)) {
                used = tag.getInt("TippedUsed");
            }

            int left = Math.max(0, maxUses - used);
            tooltip.add(Component.translatable("tooltip.overgeared.potion_uses", left, maxUses).withStyle(ChatFormatting.GRAY));
        }
        if (!ServerConfig.ENABLE_MOD_TOOLTIPS.get()) return;

        if (stack.is(Items.FLINT) && ServerConfig.GET_ROCK_USING_FLINT.get()) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.flint_flavor").withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ModItems.DIAMOND_SHARD)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.diamond_shard").withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ModItems.STONE_HAMMER_HEAD)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.stone_hammer_head").withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ModTags.Items.KNAPPABLE)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.knappable").withStyle(ChatFormatting.DARK_GRAY));
        }
        if (stack.is(ModTags.Items.HEATED_METALS)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.heatedingots.tooltip").withStyle(ChatFormatting.RED));
        }
        if (stack.is(ModTags.Items.HOT_ITEMS)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.hotitems.tooltip").withStyle(ChatFormatting.RED));
        }

        if (stack.hasTag() && stack.getTag().contains("Creator")) {
            String creatorName = stack.getTag().getString("Creator");
            Component creatorComponent = Component.translatable("tooltip.overgeared.made_by")
                    .append(" ")
                    .append(creatorName)
                    .withStyle(ChatFormatting.GRAY);
            tooltip.add(insertOffset++, creatorComponent);
        }
    }
}
