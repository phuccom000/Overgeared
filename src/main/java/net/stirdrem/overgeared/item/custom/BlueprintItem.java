package net.stirdrem.overgeared.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlueprintItem extends Item {

    public BlueprintItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        NbtCompound tag = stack.getOrCreateNbt();

        // Set default quality to POOR
        tag.putString("Quality", BlueprintQuality.POOR.name());
        tag.putInt("Uses", 0);

        // Set default tool type to first available or SWORD
        List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
        tag.putString("ToolType", !types.isEmpty() ? types.get(0).getId() : "SWORD");

        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world,
                               List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        NbtCompound tag = stack.getNbt();
        if (tag == null) return;

        // Only show quality/progress if both tags are present
        if (tag.contains("Quality")) {
            BlueprintQuality quality = getQuality(stack);

            tooltip.add(Text.translatable("tooltip.overgeared.blueprint.quality")
                    .formatted(Formatting.GRAY)
                    .append(Text.translatable(quality.getTranslationKey()).formatted(quality.getColor())));

            if (quality == BlueprintQuality.PERFECT || quality == BlueprintQuality.MASTER) {
                tooltip.add(Text.translatable("tooltip.overgeared.blueprint.maxlevel")
                        .formatted(Formatting.LIGHT_PURPLE));
            }
        }

        if (tag.contains("Uses")) {
            int uses = getUses(stack);
            int usesToLevel = getUsesToNextLevel(stack);

            if (!tag.contains("Quality") || (getQuality(stack) != BlueprintQuality.PERFECT && getQuality(stack) != BlueprintQuality.MASTER)) {
                tooltip.add(Text.translatable("tooltip.overgeared.blueprint.progress", uses, usesToLevel)
                        .formatted(Formatting.GRAY));
            }
        }

        // ToolType line only if present
        if (tag.contains("ToolType")) {
            String toolType = tag.getString("ToolType");
            tooltip.add(Text.translatable("tooltip.overgeared.blueprint.tool_type").formatted(Formatting.GRAY)
                    .append(Text.translatable("tooltype.overgeared." + toolType).formatted(Formatting.BLUE)));
        }

        if (tag.contains("Required")) {
            boolean required = tag.getBoolean("Required");

            tooltip.add(Text.translatable(
                    required
                            ? "tooltip.overgeared.blueprint.required"
                            : "tooltip.overgeared.blueprint.optional"
            ).formatted(required ? Formatting.RED : Formatting.GRAY));
        }
    }


    public static BlueprintQuality getQuality(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("Quality")) {
            return BlueprintQuality.POOR; // Default to POOR if not set
        }
        try {
            return BlueprintQuality.fromString(tag.getString("Quality"));
        } catch (IllegalArgumentException e) {
            return BlueprintQuality.POOR; // Default to POOR if invalid
        }
    }

    public static int getUses(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        return tag != null ? tag.getInt("Uses") : 0; // Default to 0 uses
    }

    public static int getUsesToNextLevel(ItemStack stack) {
        return getUsesToNextLevel(getQuality(stack));
    }

    public static ToolType getToolType(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        String id = tag.getString("ToolType");

        // Create-or-fetch instead of defaulting
        return ToolType.of(id);
    }

    private static int getUsesToNextLevel(BlueprintQuality quality) {
        return switch (quality) {
            case POOR -> BlueprintQuality.POOR.getUse();
            case WELL -> BlueprintQuality.WELL.getUse();
            case EXPERT -> BlueprintQuality.EXPERT.getUse();
            case PERFECT -> BlueprintQuality.PERFECT.getUse();
            case MASTER -> BlueprintQuality.MASTER.getUse();
        };
    }
}
