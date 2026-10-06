package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class ToolCastItem extends Item {
    private final boolean allowMaterialInsert;
    private final boolean haveDurability;

    public ToolCastItem(boolean allowMaterialInsert, boolean haveDurability, Properties settings) {
        // Max damage is a component baked in at construction, so the configured durability is
        // read once here (the config is loaded before items register).
        super(haveDurability ? settings.durability(ServerConfig.FIRED_CAST_DURABILITY.get()) : settings);
        this.allowMaterialInsert = allowMaterialInsert;
        this.haveDurability = haveDurability;
    }

    public boolean hasDurability() {
        return haveDurability;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide()) {
            return calculateAndReturnMaterials(stack, player);
        }

        return super.use(world, player, hand);
    }

    private InteractionResult calculateAndReturnMaterials(ItemStack castStack, Player player) {
        CastData data = castStack.get(ModComponents.CAST_DATA);
        if (data == null) {
            return InteractionResult.FAIL;
        }

        if (data.hasOutput()) {
            ItemStack output = data.outputStack();
            if (!player.getInventory().add(output.copy())) {
                player.drop(output.copy(), false, Prediction.SERVER_ONLY);
            }

            castStack.set(ModComponents.CAST_DATA, data.cleared());

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS,
                    0.8F,
                    1.2F
            );
            return InteractionResult.SUCCESS;
        }

        List<ItemStack> inputItems = data.inputStacks();
        if (inputItems.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.overgeared.cast_empty"));
            return InteractionResult.FAIL;
        }

        for (ItemStack inputItem : inputItems) {
            if (!player.getInventory().add(inputItem.copy())) {
                player.drop(inputItem.copy(), false, Prediction.SERVER_ONLY);
            }
        }

        castStack.set(ModComponents.CAST_DATA, data.cleared());
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack castStack, Slot slot, ClickAction clickType, Player player) {
        if (!allowMaterialInsert) return false;
        if (clickType != ClickAction.SECONDARY) return false;
        if (!slot.mayPickup(player)) return false;

        ItemStack slotStack = slot.getItem();
        if (slotStack.isEmpty()) return false;

        if (insertMaterial(castStack, slotStack, player)) {
            slotStack.shrink(1);
            slot.setChanged();
            return true;
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack castStack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        if (!allowMaterialInsert) return false;
        if (clickType != ClickAction.SECONDARY) return false;

        if (insertMaterial(castStack, otherStack, player)) {
            otherStack.shrink(1);
            return true;
        }

        return false;
    }

    private boolean insertMaterial(ItemStack cast, ItemStack material, Player player) {
        CastData data = cast.getOrDefault(ModComponents.CAST_DATA, CastData.EMPTY);
        if (data.hasOutput()) {
            return false;
        }
        if (material.isEmpty()) return false;

        if (!ConfigHelper.isValidMaterial(material)) {
            player.sendOverlayMessage(Component.translatable("message.overgeared.invalid_material"));
            return false;
        }

        int value = ConfigHelper.getMaterialValue(material);
        if (value <= 0) return false;

        if (data.wouldOverflow(value)) {
            return false;
        }

        String mat = ConfigHelper.getMaterialForItem(material);
        cast.set(ModComponents.CAST_DATA, data.withAddedMaterial(mat, value, material));

        playInsertSound(player);
        return true;
    }

    private void playInsertSound(Player player) {
        player.level().playSound(
                player,
                player.blockPosition(),
                SoundEvents.BUNDLE_INSERT,
                SoundSource.PLAYERS,
                0.7F, 1.1F
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        CastData data = stack.get(ModComponents.CAST_DATA);
        if (data == null) return;

        String quality = data.quality();
        if (!quality.isEmpty() && !quality.equals("NONE")) {
            ChatFormatting color = BlueprintQuality.getColor(quality);
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.tool_cast.quality")
                            .append(" ")
                            .append(
                                    Component.translatable("quality.overgeared." + quality.toLowerCase(Locale.ROOT))
                                            .withStyle(color)
                            )
                            .withStyle(ChatFormatting.GRAY)
            );
        }

        if (!data.toolType().isEmpty()) {
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.tool_cast.type")
                            .append(" ")
                            .append(Component.translatable("tooltype.overgeared." + data.toolType().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.BLUE))
                            .withStyle(ChatFormatting.GRAY)
            );
        }

        if (!data.materials().isEmpty()) {
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.tool_cast.materials")
                            .withStyle(ChatFormatting.GRAY)
            );

            for (Map.Entry<String, Integer> entry : data.materials().entrySet()) {
                String key = entry.getKey();
                Component name = Component.translatable("material.overgeared." + key.toLowerCase(Locale.ROOT));
                if (name.getString().equals("material.overgeared." + key.toLowerCase(Locale.ROOT))) {
                    name = Component.literal(key);
                }

                tooltip.accept(
                        Component.literal("  • ").append(name)
                                .append(Component.literal(": " + entry.getValue()))
                                .withStyle(ChatFormatting.WHITE)
                );
            }
        }

        int raw = data.amount();
        double amt = raw / 9.0;
        int maxRaw = data.maxAmount() > 0 ? data.maxAmount() : raw;
        double maxAmt = maxRaw / 9.0;

        tooltip.accept(
                Component.translatable("tooltip.overgeared.tool_cast.amount")
                        .append(" ")
                        .append(
                                Component.literal(String.format("%.2f", amt))
                                        .withStyle(ChatFormatting.YELLOW)
                        )
                        .append(" / ")
                        .append(
                                Component.literal(String.format("%.2f", maxAmt))
                                        .withStyle(ChatFormatting.WHITE)
                        )
                        .withStyle(ChatFormatting.GRAY)
        );
        if (amt / maxAmt != 1) {
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.add_materials")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
            );
        }

        if (data.hasOutput()) {
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.tool_cast.contains")
                            .withStyle(ChatFormatting.GRAY)
            );
            tooltip.accept(
                    Component.literal("  • ")
                            .append(data.outputStack().getHoverName())
                            .withStyle(ChatFormatting.GOLD)
            );
        }

        if (!data.materials().isEmpty() || !data.input().isEmpty() || data.hasOutput()) {
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.cast_right_click")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
            );
        }
    }
}
