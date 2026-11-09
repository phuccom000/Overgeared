package net.stirdrem.overgeared.item.custom;
import net.minecraft.util.Prediction;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.InteractionResult;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolCastItem extends Item {
    private final boolean haveDurability;

    public ToolCastItem(boolean haveDurability, Properties props) {
        super(props);
        this.haveDurability = haveDurability;
    }

    @Override
    public Component getName(ItemStack stack) {
        CastData castData = stack.getOrDefault(ModComponents.CAST_DATA.get(), CastData.EMPTY);
        if (!castData.toolType().isEmpty()) {
            String name = getDescriptionId().replace("item", "cast");
            Component toolType = Component
                    .translatable("tooltype.overgeared." + castData.toolType().toLowerCase(java.util.Locale.ROOT));
            return Component.translatable(name, toolType);
        }
        return super.getName(stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            return calculateAndReturnMaterials(stack, player, hand);
        }

        return super.use(level, player, hand);
    }

    private InteractionResult calculateAndReturnMaterials(ItemStack castStack, Player player,
            InteractionHand hand) {
        CastData data = castStack.getOrDefault(ModComponents.CAST_DATA.get(), CastData.EMPTY);
        boolean heated = castStack.getOrDefault(ModComponents.HEATED_COMPONENT.get(), false);

        // Return Output if exists
        if (data.hasOutput()) {
            ItemStack output = data.output();
            if (heated) {
                output.set(ModComponents.HEATED_COMPONENT.get(), true);
                castStack.set(ModComponents.HEATED_COMPONENT.get(), false);
            }

            if (!player.getInventory().add(output.copy())) {
                player.drop(output.copy(), false, Prediction.SERVER_ONLY);
            }

            castStack.set(ModComponents.CAST_DATA.get(), data.cleared());

            player.level().playSound(
                    null, player.blockPosition(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                    0.8F, 1.2F);

            return InteractionResult.SUCCESS;
        }

        // Return input materials
        List<ItemStack> inputItems = data.input();
        if (inputItems.isEmpty()) {
            player.sendOverlayMessage(
                    Component.translatable("message.overgeared.cast_empty"));
            return InteractionResult.FAIL;
        }

        for (ItemStack inputItem : inputItems) {
            if (!player.getInventory().add(inputItem.copy())) {
                player.drop(inputItem.copy(), false, Prediction.SERVER_ONLY);
            }
        }

        castStack.set(ModComponents.CAST_DATA.get(), data.withoutInputs());

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack castStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY)
            return false;
        if (!slot.allowModification(player))
            return false;

        ItemStack slotStack = slot.getItem();
        if (slotStack.isEmpty())
            return false;

        if (insertMaterial(castStack, slotStack, player)) {
            slot.remove(1);
            return true;
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack castStack, ItemStack otherStack, Slot slot, ClickAction action,
            Player player, SlotAccess slotAccess) {
        if (action != ClickAction.SECONDARY)
            return false;

        if (insertMaterial(castStack, otherStack, player)) {
            otherStack.shrink(1);
            return true;
        }

        return false;
    }

    private boolean insertMaterial(ItemStack cast, ItemStack material, Player player) {
        CastData data = cast.getOrDefault(ModComponents.CAST_DATA.get(), CastData.EMPTY);

        if (data.hasOutput())
            return false;
        if (material.isEmpty())
            return false;

        if (!ConfigHelper.isValidMaterial(material)) {
            player.sendOverlayMessage(Component.translatable("message.overgeared.invalid_material"));
            return false;
        }

        int value = ConfigHelper.getMaterialValue(material);
        if (value <= 0)
            return false;

        int amount = data.amount();
        int maxAmount = data.maxAmount() > 0 ? data.maxAmount() : Integer.MAX_VALUE;

        if (amount + value > maxAmount) {
            return false;
        }

        String mat = ConfigHelper.getMaterialForItem(material);

        // Update materials map and add input
        ItemStack singleMaterial = material.copy();
        singleMaterial.setCount(1);

        CastData newData = data.withAddedMaterial(mat, value).withAddedInput(singleMaterial);
        cast.set(ModComponents.CAST_DATA.get(), newData);

        playInsertSound(player);
        return true;
    }

    private void playInsertSound(Player player) {
        player.level().playSound(
                player, player.blockPosition(),
                SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS,
                0.7F, 1.1F);
    }

    private List<ItemStack> getInputItemsFromCast(ItemStack cast) {
        CastData data = cast.getOrDefault(ModComponents.CAST_DATA.get(), CastData.EMPTY);
        return data.input();
    }

    public boolean isDamageable(ItemStack stack) {
        return haveDurability;
    }

    public int getMaxDamage(ItemStack stack) {
        return haveDurability ? ServerConfig.FIRED_CAST_DURABILITY.get() : 0;
    }

    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);

        CastData data = stack.getOrDefault(ModComponents.CAST_DATA.get(), CastData.EMPTY);

        if (!data.quality().isEmpty()) {
            String quality = data.quality();
            ChatFormatting color = ForgingQuality.getColor(quality);
            if (!quality.equals("NONE"))
                tooltip.accept(
                        Component.translatable("tooltip.overgeared.tool_cast.quality")
                                .append(" ")
                                .append(
                                        Component
                                                .translatable("quality.overgeared."
                                                        + quality.toLowerCase(java.util.Locale.ROOT))
                                                .withStyle(color))
                                .withStyle(ChatFormatting.GRAY));
        }

        if (!data.toolType().isEmpty()) {
            String toolType = data.toolType();
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.tool_cast.type")
                            .append(" ")
                            .append(Component
                                    .translatable("tooltype.overgeared." + toolType.toLowerCase(java.util.Locale.ROOT))
                                    .withStyle(ChatFormatting.BLUE))
                            .withStyle(ChatFormatting.GRAY));
        }

        // If the cast has an output, only show output
        if (data.hasOutput()) {
            ItemStack output = data.output();
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.tool_cast.contains")
                            .withStyle(ChatFormatting.GRAY));
            tooltip.accept(
                    Component.literal("  • ")
                            .append(output.getHoverName())
                            .withStyle(ChatFormatting.GOLD));
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.cast_right_click")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }

        if (!data.materials().isEmpty()) {
            Map<String, Integer> materials = data.materials();
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.tool_cast.materials")
                            .withStyle(ChatFormatting.GRAY));

            for (Map.Entry<String, Integer> entry : materials.entrySet()) {
                String key = entry.getKey();
                int amount = entry.getValue();

                Component display = Component
                        .translatable("material.overgeared." + key.toLowerCase(java.util.Locale.ROOT));
                if (display.getString().equals("material.overgeared." + key.toLowerCase(java.util.Locale.ROOT))) {
                    display = Component.literal(key);
                }

                tooltip.accept(
                        Component.literal("  • ").append(display)
                                .append(Component.literal(": " + amount))
                                .withStyle(ChatFormatting.WHITE));
            }
        }

        if (data.amount() > 0 || data.maxAmount() > 0) {
            int raw = data.amount();
            double amt = raw / 9.0;

            int maxRaw = data.maxAmount() > 0 ? data.maxAmount() : raw;
            double maxAmt = maxRaw / 9.0;

            tooltip.accept(
                    Component.translatable("tooltip.overgeared.tool_cast.amount")
                            .append(" ")
                            .append(Component.literal(String.format("%.2f", amt))
                                    .withStyle(ChatFormatting.YELLOW))
                            .append(" / ")
                            .append(Component.literal(String.format("%.2f", maxAmt))
                                    .withStyle(ChatFormatting.WHITE))
                            .withStyle(ChatFormatting.GRAY));
            if (amt / maxAmt != 1)
                tooltip.accept(
                        Component.translatable("tooltip.overgeared.add_materials")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        if (!data.materials().isEmpty() || !data.input().isEmpty() || data.hasOutput()) {
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.cast_right_click")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

    }

    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }
}