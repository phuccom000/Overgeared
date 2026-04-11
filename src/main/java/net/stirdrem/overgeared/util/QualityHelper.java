package net.stirdrem.overgeared.util;

import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.config.ServerConfig;

public class QualityHelper {
    public static float getDurabilityMultiplier(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            String quality = stack.getTag().getString("ForgingQuality");
            return switch (quality) {
                case "poor" -> ServerConfig.POOR_DURABILITY_BONUS.get().floatValue();  // 30% worse
                case "well" -> ServerConfig.WELL_DURABILITY_BONUS.get().floatValue();  // 10% better
                case "expert" -> ServerConfig.EXPERT_DURABILITY_BONUS.get().floatValue(); // 30% better
                case "perfect" -> ServerConfig.PERFECT_DURABILITY_BONUS.get().floatValue(); // 50% better
                case "master" -> ServerConfig.MASTER_DURABILITY_BONUS.get().floatValue(); // 50% better
                default -> 1.0f;
            };
        }
        return 1.0f;
    }

    public static float getMiningSpeedMultiplier(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            String quality = stack.getTag().getString("ForgingQuality");
            return switch (quality) {
                case "poor" -> ServerConfig.POOR_MINING_SPEED_BONUS.get().floatValue();  // 30% worse
                case "well" -> ServerConfig.WELL_MINING_SPEED_BONUS.get().floatValue();  // 10% better
                case "expert" -> ServerConfig.EXPERT_MINING_SPEED_BONUS.get().floatValue(); // 30% better
                case "perfect" -> ServerConfig.PERFECT_MINING_SPEED_BONUS.get().floatValue(); // 50% better
                case "master" -> ServerConfig.MASTER_MINING_SPEED_BONUS.get().floatValue(); // 50% better
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
