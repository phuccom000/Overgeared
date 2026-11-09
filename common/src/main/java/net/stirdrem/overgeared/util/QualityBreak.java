package net.stirdrem.overgeared.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;

/**
 * Quality-based chance for an item to survive breaking. Called at the start of
 * {@code ItemStack#hurtAndBreak(int, LivingEntity, EquipmentSlot)} (see ItemStackBreakMixin).
 */
public final class QualityBreak {
    private QualityBreak() {
    }

    /**
     * @return true when the item was saved from breaking and the vanilla logic should be skipped
     */
    public static boolean preventBreak(ItemStack stack, int amount, ServerLevel level, LivingEntity owner, EquipmentSlot slot) {
        if (!stack.isDamageableItem()) return false;
        if (owner instanceof Player player && player.hasInfiniteMaterials()) return false;

        int currentDamage = stack.getDamageValue();
        int newDamage = currentDamage + amount;
        int max = stack.getMaxDamage();
        RandomSource random = level.getRandom();
        if (currentDamage < max && newDamage >= max) {
            if (!ServerConfig.ENABLE_QUALITY_BREAK_SYSTEM.get()) {
                return false;
            }

            if (random.nextFloat() < getBreakChance(stack)) {
                return false;
            }

            // Stays "broken" at max damage and drops a quality tier instead of being destroyed
            stack.setDamageValue(max);
            ForgingQuality.downgrade(stack);

            // entity event -> clients play the break sound and particles
            owner.onEquippedItemBroken(stack, slot);
            return true;
        }
        return false;
    }

    private static float getBreakChance(ItemStack stack) {
        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY.get());
        if (quality == null) {
            return ServerConfig.BREAK_CHANCE_WELL.get().floatValue();
        }

        return switch (quality) {
            case POOR -> ServerConfig.BREAK_CHANCE_POOR.get().floatValue();
            case EXPERT -> ServerConfig.BREAK_CHANCE_EXPERT.get().floatValue();
            case PERFECT -> ServerConfig.BREAK_CHANCE_PERFECT.get().floatValue();
            case MASTER -> ServerConfig.BREAK_CHANCE_MASTER.get().floatValue();
            default -> ServerConfig.BREAK_CHANCE_WELL.get().floatValue();
        };
    }
}
