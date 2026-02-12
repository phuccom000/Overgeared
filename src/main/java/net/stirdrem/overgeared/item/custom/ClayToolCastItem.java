package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;

import java.util.List;

public class ClayToolCastItem extends Item {
    public ClayToolCastItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        CastData castData = stack.getOrDefault(ModComponents.CAST_DATA, CastData.EMPTY);
        if (!castData.toolType().isEmpty()) {
            String name = super.getDescriptionId(stack).replace("item", "cast");
            Component toolType = Component.translatable("tooltype.overgeared." + castData.toolType().toLowerCase());
            return Component.translatable(name, toolType);
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltip, tooltipFlag);

        tooltip.add(Component.translatable("tooltip.overgeared.unfired_cast_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        CastData castData = stack.getOrDefault(ModComponents.CAST_DATA, CastData.EMPTY);

        if (!castData.quality().isEmpty() && !castData.quality().equals("NONE")) {
            String quality = castData.quality();
            ChatFormatting color = BlueprintQuality.getColor(quality);
            tooltip.add(
                    Component.translatable("tooltip.overgeared.tool_cast.quality")
                            .append(" ")
                            .append(Component.translatable("quality.overgeared." + quality.toLowerCase())
                                    .withStyle(color))
                            .withStyle(ChatFormatting.GRAY)
            );
        }

        if (!castData.toolType().isEmpty()) {
            String toolType = castData.toolType();
            tooltip.add(
                    Component.translatable("tooltip.overgeared.tool_cast.type")
                            .append(" ")
                            .append(Component.translatable("tooltype.overgeared." + toolType.toLowerCase())
                                    .withStyle(ChatFormatting.BLUE))
                            .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }
}
