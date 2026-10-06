package net.stirdrem.overgeared.util;

import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.config.ServerConfig;

public class QualityHelper {
    public static float getDurabilityMultiplier(ItemStack stack) {
        ForgingQuality quality = ForgingQuality.get(stack);
        if (quality == null) return 1.0f;
        return switch (quality) {
            case POOR -> ServerConfig.POOR_DURABILITY_BONUS.get().floatValue();
            case WELL -> ServerConfig.WELL_DURABILITY_BONUS.get().floatValue();
            case EXPERT -> ServerConfig.EXPERT_DURABILITY_BONUS.get().floatValue();
            case PERFECT -> ServerConfig.PERFECT_DURABILITY_BONUS.get().floatValue();
            case MASTER -> ServerConfig.MASTER_DURABILITY_BONUS.get().floatValue();
            default -> 1.0f;
        };
    }

    public static float getMiningSpeedMultiplier(ItemStack stack) {
        ForgingQuality quality = ForgingQuality.get(stack);
        if (quality == null) return 1.0f;
        return switch (quality) {
            case POOR -> ServerConfig.POOR_MINING_SPEED_BONUS.get().floatValue();
            case WELL -> ServerConfig.WELL_MINING_SPEED_BONUS.get().floatValue();
            case EXPERT -> ServerConfig.EXPERT_MINING_SPEED_BONUS.get().floatValue();
            case PERFECT -> ServerConfig.PERFECT_MINING_SPEED_BONUS.get().floatValue();
            case MASTER -> ServerConfig.MASTER_MINING_SPEED_BONUS.get().floatValue();
            default -> 1.0f;
        };
    }

    private static boolean calculatingAttributes = false;

    public static boolean isCalculatingAttributes() {
        return calculatingAttributes;
    }

    public static void setCalculatingAttributes(boolean state) {
        calculatingAttributes = state;
    }
}
