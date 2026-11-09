package net.stirdrem.overgeared.item.custom;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;

import java.util.List;

public class ClayToolCastItem extends Item {
    public ClayToolCastItem(Properties properties) {
        super(properties);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltip, tooltipFlag);

        tooltip.accept(Component.translatable("tooltip.overgeared.unfired_cast_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        CastData castData = stack.getOrDefault(ModComponents.CAST_DATA.get(), CastData.EMPTY);

        if (!castData.quality().isEmpty() && !castData.quality().equals("NONE")) {
            String quality = castData.quality();
            ChatFormatting color = ForgingQuality.getColor(quality);
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.tool_cast.quality")
                            .append(" ")
                            .append(Component
                                    .translatable("quality.overgeared." + quality.toLowerCase(java.util.Locale.ROOT))
                                    .withStyle(color))
                            .withStyle(ChatFormatting.GRAY));
        }

        if (!castData.toolType().isEmpty()) {
            String toolType = castData.toolType();
            tooltip.accept(
                    Component.translatable("tooltip.overgeared.tool_cast.type")
                            .append(" ")
                            .append(Component
                                    .translatable("tooltype.overgeared." + toolType.toLowerCase(java.util.Locale.ROOT))
                                    .withStyle(ChatFormatting.BLUE))
                            .withStyle(ChatFormatting.GRAY));
        }
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
