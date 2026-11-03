package net.stirdrem.overgeared.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import org.jetbrains.annotations.Nullable;

/**
 * Quality-based chance for an item to survive breaking. Called at the start of
 * {@code ItemStack#hurtAndBreak}, whose signature differs between NeoForge and vanilla, so each loader
 * has its own mixin.
 */
public final class QualityBreak {
    private QualityBreak() {
    }

    /**
     * @return true when the item was saved from breaking and the vanilla logic should be skipped
     */
    public static boolean preventBreak(ItemStack stack, int amount, ServerLevel level, @Nullable LivingEntity entity) {
        int currentDamage = stack.getDamageValue();
        int newDamage = currentDamage + amount;
        int max = stack.getMaxDamage();
        RandomSource random = level.random;
        if (currentDamage < max && newDamage >= max) {
            if (!ServerConfig.ENABLE_QUALITY_BREAK_SYSTEM.get()) {
                return false;
            }

            float breakChance = getBreakChance(stack);

            if (random.nextFloat() < breakChance) {
                return false;
            }

            stack.setDamageValue(max);
            ForgingQuality.downgrade(stack);

            if (stack.getDamageValue() < 0) {
                stack.setDamageValue(0);
            } else if (stack.getDamageValue() > stack.getMaxDamage()) {
                stack.setDamageValue(stack.getMaxDamage());
            }

            if (entity instanceof Player player) {
                InteractionHand hand = player.getMainHandItem() == stack
                        ? InteractionHand.MAIN_HAND
                        : InteractionHand.OFF_HAND;

                player.onEquippedItemBroken(stack.getItem(), LivingEntity.getSlotForHand(hand));
            } else if (entity != null) {
                entity.level().playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        SoundEvents.ITEM_BREAK,
                        SoundSource.PLAYERS,
                        0.8F,
                        0.8F + entity.level().random.nextFloat() * 0.4F
                );
            }

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
