package net.stirdrem.overgeared.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

    private static void onTooltip(ItemStack stack, net.minecraft.client.item.TooltipContext context, List<Text> tooltip) {
        int insertOffset = 1;

        // Add Forging Quality
        if (stack.hasNbt() && stack.getNbt().contains("ForgingQuality")) {
            String quality = stack.getNbt().getString("ForgingQuality");
            Text qualityComponent = switch (quality) {
                case "poor" -> Text.translatable("tooltip.overgeared.poor").formatted(Formatting.RED);
                case "well" -> Text.translatable("tooltip.overgeared.well").formatted(Formatting.YELLOW);
                case "expert" -> Text.translatable("tooltip.overgeared.expert").formatted(Formatting.BLUE);
                case "perfect" -> Text.translatable("tooltip.overgeared.perfect").formatted(Formatting.GOLD);
                case "master" -> Text.translatable("tooltip.overgeared.master").formatted(Formatting.LIGHT_PURPLE);
                default -> null;
            };
            if (qualityComponent != null) {
                tooltip.add(insertOffset++, qualityComponent);
            }
        }
        if (isBroken(stack) && ServerConfig.ENABLE_MOD_TOOLTIPS.get()) {
            tooltip.add(insertOffset++,
                    Text.translatable("tooltip.overgeared.item_broken").formatted(Formatting.RED)
            );
        }
        if (stack.hasNbt() && stack.getNbt().contains("Heated")) {
            tooltip.add(insertOffset++, Text.translatable("tooltip.overgeared.heated").formatted(Formatting.RED, Formatting.ITALIC));
        }
        // Add Polish status
        if (stack.hasNbt() && stack.getNbt().contains("Polished")) {
            boolean isPolished = stack.getNbt().getBoolean("Polished");
            Text polishComponent = isPolished
                    ? Text.translatable("tooltip.overgeared.polished").formatted(Formatting.BLUE, Formatting.ITALIC)
                    : Text.translatable("tooltip.overgeared.unpolished").formatted(Formatting.RED, Formatting.ITALIC);
            tooltip.add(insertOffset++, polishComponent);
        }
        if (stack.hasNbt() && stack.getNbt().contains("failedResult")) {
            tooltip.add(insertOffset, Text.translatable("tooltip.overgeared.failedResult").formatted(Formatting.RED));
        }

        // Smithing Hammer special tooltip
        if (stack.isIn(ModTags.Items.SMITHING_HAMMERS)) {
            if (!Screen.hasShiftDown()) {
                tooltip.add(insertOffset, Text.translatable("tooltip.overgeared.smithing_hammer.hold_shift")
                        .formatted(Formatting.GRAY, Formatting.ITALIC));
            } else {
                tooltip.add(insertOffset++, Text.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line1")
                        .formatted(Formatting.GRAY));
                tooltip.add(insertOffset++, Text.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line2")
                        .formatted(Formatting.GRAY));
                if (ServerConfig.ENABLE_STONE_TO_ANVIL.get())
                    tooltip.add(insertOffset++, Text.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line3")
                            .formatted(Formatting.GRAY));
                if (ServerConfig.ENABLE_ANVIL_TO_SMITHING.get())
                    tooltip.add(insertOffset++, Text.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line4")
                            .formatted(Formatting.GRAY));
            }
        }

        if (stack.isOf(Items.POTION) && ServerConfig.TIPPING_TOGGLE.get()) {
            NbtCompound tag = stack.getNbt();
            int maxUses = ServerConfig.MAX_POTION_TIPPING_USE.get();
            int used = 0;

            if (tag != null && tag.contains("TippedUsed", NbtElement.INT_TYPE)) {
                used = tag.getInt("TippedUsed");
            }

            int left = Math.max(0, maxUses - used);
            tooltip.add(Text.translatable("tooltip.overgeared.potion_uses", left, maxUses).formatted(Formatting.GRAY));
        }
        if (!ServerConfig.ENABLE_MOD_TOOLTIPS.get()) return;

        if (stack.isOf(Items.FLINT) && ServerConfig.GET_ROCK_USING_FLINT.get()) {
            tooltip.add(insertOffset++, Text.translatable("tooltip.overgeared.flint_flavor").formatted(Formatting.GRAY));
        }
        if (stack.isOf(ModItems.DIAMOND_SHARD)) {
            tooltip.add(insertOffset++, Text.translatable("tooltip.overgeared.diamond_shard").formatted(Formatting.GRAY));
        }
        if (stack.isOf(ModItems.STONE_HAMMER_HEAD)) {
            tooltip.add(insertOffset++, Text.translatable("tooltip.overgeared.stone_hammer_head").formatted(Formatting.GRAY));
        }
        if (stack.isIn(ModTags.Items.KNAPPABLE)) {
            tooltip.add(insertOffset++, Text.translatable("tooltip.overgeared.knappable").formatted(Formatting.DARK_GRAY));
        }
        if (stack.isIn(ModTags.Items.HEATED_METALS)) {
            tooltip.add(insertOffset++, Text.translatable("tooltip.overgeared.heatedingots.tooltip").formatted(Formatting.RED));
        }
        if (stack.isIn(ModTags.Items.HOT_ITEMS)) {
            tooltip.add(insertOffset++, Text.translatable("tooltip.overgeared.hotitems.tooltip").formatted(Formatting.RED));
        }

        if (stack.hasNbt() && stack.getNbt().contains("Creator")) {
            String creatorName = stack.getNbt().getString("Creator");
            Text creatorComponent = Text.translatable("tooltip.overgeared.made_by")
                    .append(" ")
                    .append(creatorName)
                    .formatted(Formatting.GRAY);
            tooltip.add(insertOffset++, creatorComponent);
        }
    }
}
