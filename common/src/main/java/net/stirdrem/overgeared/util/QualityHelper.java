package net.stirdrem.overgeared.util;

import net.stirdrem.overgeared.Overgeared;

import net.minecraft.world.item.ItemStack;

public class QualityHelper {
    public static float getDurabilityMultiplier(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            String quality = stack.getTag().getString("ForgingQuality");
            return switch (quality) {
                case "poor" -> (float) Overgeared.getConfig().POOR_DURABILITY_BONUS();
                case "well" -> (float) Overgeared.getConfig().WELL_DURABILITY_BONUS();
                case "expert" -> (float) Overgeared.getConfig().EXPERT_DURABILITY_BONUS();
                case "perfect" -> (float) Overgeared.getConfig().PERFECT_DURABILITY_BONUS();
                case "master" -> (float) Overgeared.getConfig().MASTER_DURABILITY_BONUS();
                default -> 1.0f;
            };
        }
        return 1.0f;
    }

    public static float getMiningSpeedMultiplier(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            String quality = stack.getTag().getString("ForgingQuality");
            return switch (quality) {
                case "poor" -> (float) Overgeared.getConfig().POOR_MINING_SPEED_BONUS();
                case "well" -> (float) Overgeared.getConfig().WELL_MINING_SPEED_BONUS();
                case "expert" -> (float) Overgeared.getConfig().EXPERT_MINING_SPEED_BONUS();
                case "perfect" -> (float) Overgeared.getConfig().PERFECT_MINING_SPEED_BONUS();
                case "master" -> (float) Overgeared.getConfig().MASTER_MINING_SPEED_BONUS();
                default -> 1.0f;
            };
        }
        return 1.0f;
    }

    private static boolean calculatingAttributes = false;

    public static boolean isCalculatingAttributes() {
        return calculatingAttributes;
    }

    public static void setCalculatingAttributes(boolean state) {
        calculatingAttributes = state;
    }
}
