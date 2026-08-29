package net.stirdrem.overgeared.util;

import net.minecraft.item.ItemStack;
import net.stirdrem.overgeared.config.ServerConfig;

public class QualityHelper {
    public static float getDurabilityMultiplier(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains("ForgingQuality")) {
            String quality = stack.getNbt().getString("ForgingQuality");
            return switch (quality) {
                case "poor" -> ServerConfig.POOR_DURABILITY_BONUS.get().floatValue();
                case "well" -> ServerConfig.WELL_DURABILITY_BONUS.get().floatValue();
                case "expert" -> ServerConfig.EXPERT_DURABILITY_BONUS.get().floatValue();
                case "perfect" -> ServerConfig.PERFECT_DURABILITY_BONUS.get().floatValue();
                case "master" -> ServerConfig.MASTER_DURABILITY_BONUS.get().floatValue();
                default -> 1.0f;
            };
        }
        return 1.0f;
    }

    public static float getMiningSpeedMultiplier(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains("ForgingQuality")) {
            String quality = stack.getNbt().getString("ForgingQuality");
            return switch (quality) {
                case "poor" -> ServerConfig.POOR_MINING_SPEED_BONUS.get().floatValue();
                case "well" -> ServerConfig.WELL_MINING_SPEED_BONUS.get().floatValue();
                case "expert" -> ServerConfig.EXPERT_MINING_SPEED_BONUS.get().floatValue();
                case "perfect" -> ServerConfig.PERFECT_MINING_SPEED_BONUS.get().floatValue();
                case "master" -> ServerConfig.MASTER_MINING_SPEED_BONUS.get().floatValue();
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
