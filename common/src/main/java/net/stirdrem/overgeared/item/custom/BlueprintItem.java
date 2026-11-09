package net.stirdrem.overgeared.item.custom;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;

import java.util.List;

public class BlueprintItem extends Item {

    public BlueprintItem(Properties properties) {
        super(properties);
    }

    // Item#verifyComponentsAfterLoad is gone; blueprints without data are handled where they are read.

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        BlueprintData data = stack.get(ModComponents.BLUEPRINT_DATA.get());
        if (data == null) return;

        ForgingQuality quality = data.getQualityEnum();

        // Only show quality & progress if not NONE
        if (quality != ForgingQuality.NONE) {
            // Show quality
            tooltip.accept(Component.translatable("tooltip.overgeared.blueprint.quality")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(quality.getBlueprintTranslationKey()).withStyle(quality.getColor())));

            if (quality.isMaxBlueprintLevel()) {
                tooltip.accept(Component.translatable("tooltip.overgeared.blueprint.maxlevel")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                // Show progress
                tooltip.accept(Component.translatable(
                        "tooltip.overgeared.blueprint.progress",
                        data.uses(),
                        getUsesToNextLevel(quality)
                ).withStyle(ChatFormatting.GRAY));
            }
        }

        // Show tool type (always shown)
        ToolType toolType = getToolType(stack);
        tooltip.accept(Component.translatable("tooltip.overgeared.blueprint.tool_type")
                .withStyle(ChatFormatting.GRAY)
                .append(toolType.getDisplayName().withStyle(ChatFormatting.BLUE)));

        // Show required status
        if (stack.has(ModComponents.BLUEPRINT_REQUIRED.get())) {
            if (Boolean.TRUE.equals(stack.get(ModComponents.BLUEPRINT_REQUIRED.get())))
                tooltip.accept(Component.translatable("tooltip.overgeared.blueprint.required")
                        .withStyle(ChatFormatting.RED));
            else tooltip.accept(Component.translatable("tooltip.overgeared.blueprint.optional")
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    public static ForgingQuality getQuality(ItemStack stack) {
        BlueprintData data = stack.get(ModComponents.BLUEPRINT_DATA.get());
        if (data == null) return ForgingQuality.POOR;
        return data.getQualityEnum();
    }

    public static int getUses(ItemStack stack) {
        BlueprintData data = stack.get(ModComponents.BLUEPRINT_DATA.get());
        return data == null ? 0 : data.uses();
    }

    public static int getUsesToNextLevel(ItemStack stack) {
        return getUsesToNextLevel(getQuality(stack));
    }

    public static ToolType getToolType(ItemStack stack) {
        BlueprintData data = stack.get(ModComponents.BLUEPRINT_DATA.get());
        if (data == null || data.toolType().isEmpty()) {
            List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
            return !types.isEmpty() ? types.getFirst() : ToolType.SWORD;
        }
        return ToolTypeRegistry.byId(data.toolType()).orElse(ToolType.SWORD);
    }

    public static void setDefaultData(ItemStack stack) {
        List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
        String defaultToolType = !types.isEmpty() ? types.getFirst().getId() : "sword";

        BlueprintData data = new BlueprintData(
                ForgingQuality.POOR.name(),
                defaultToolType,
                0
        );
        stack.set(ModComponents.BLUEPRINT_DATA.get(), data);
    }

    public static void cycleToolType(ItemStack stack) {
        List<ToolType> available = ToolTypeRegistry.getRegisteredTypesAll();
        if (available.isEmpty()) return;

        ToolType current = getToolType(stack);
        int currentIndex = available.indexOf(current);
        int nextIndex = (currentIndex + 1) % available.size();

        BlueprintData data = stack.getOrDefault(ModComponents.BLUEPRINT_DATA.get(), BlueprintData.createDefault());
        stack.set(ModComponents.BLUEPRINT_DATA.get(), data.withToolType(available.get(nextIndex).getId()));
    }

    private static int getUsesToNextLevel(ForgingQuality quality) {
        return quality.getBlueprintUses();
    }
}