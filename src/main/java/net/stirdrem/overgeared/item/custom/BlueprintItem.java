package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;

import java.util.List;
import java.util.function.Consumer;

public class BlueprintItem extends Item {

    public BlueprintItem(Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        // Default quality POOR, 0 uses, first registered tool type (or sword)
        List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
        stack.set(ModComponents.BLUEPRINT_DATA, new BlueprintData(
                BlueprintQuality.POOR.name(), !types.isEmpty() ? types.get(0).getId() : "SWORD", 0));
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        BlueprintData data = stack.get(ModComponents.BLUEPRINT_DATA);
        if (data != null) {
            BlueprintQuality quality = data.getQualityEnum();
            tooltip.accept(Component.translatable("tooltip.overgeared.blueprint.quality")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(quality.getTranslationKey()).withStyle(quality.getColor())));

            if (quality == BlueprintQuality.PERFECT || quality == BlueprintQuality.MASTER) {
                tooltip.accept(Component.translatable("tooltip.overgeared.blueprint.maxlevel")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                tooltip.accept(Component.translatable("tooltip.overgeared.blueprint.progress", data.uses(), getUsesToNextLevel(quality))
                        .withStyle(ChatFormatting.GRAY));
            }

            tooltip.accept(Component.translatable("tooltip.overgeared.blueprint.tool_type").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("tooltype.overgeared." + data.toolType()).withStyle(ChatFormatting.BLUE)));
        }

        Boolean required = stack.get(ModComponents.BLUEPRINT_REQUIRED);
        if (required != null) {
            tooltip.accept(Component.translatable(
                    required
                            ? "tooltip.overgeared.blueprint.required"
                            : "tooltip.overgeared.blueprint.optional"
            ).withStyle(required ? ChatFormatting.RED : ChatFormatting.GRAY));
        }
    }

    public static BlueprintData getData(ItemStack stack) {
        return stack.getOrDefault(ModComponents.BLUEPRINT_DATA, BlueprintData.createDefault());
    }

    public static BlueprintQuality getQuality(ItemStack stack) {
        BlueprintData data = stack.get(ModComponents.BLUEPRINT_DATA);
        return data != null ? data.getQualityEnum() : BlueprintQuality.POOR; // Default to POOR if not set
    }

    public static int getUses(ItemStack stack) {
        BlueprintData data = stack.get(ModComponents.BLUEPRINT_DATA);
        return data != null ? data.uses() : 0; // Default to 0 uses
    }

    public static int getUsesToNextLevel(ItemStack stack) {
        return getUsesToNextLevel(getQuality(stack));
    }

    public static ToolType getToolType(ItemStack stack) {
        // Create-or-fetch instead of defaulting
        return ToolType.of(getData(stack).toolType());
    }

    private static int getUsesToNextLevel(BlueprintQuality quality) {
        return quality.getUse();
    }
}
