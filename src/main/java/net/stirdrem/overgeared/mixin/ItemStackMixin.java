package net.stirdrem.overgeared.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ItemUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(
            method = "getDestroySpeed",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (isBroken(stack)) {
            cir.setReturnValue(0.0F);
            return;
        }

        float baseSpeed = cir.getReturnValueF();

        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY);
        if (quality != null) {
            float multiplier = quality.getMiningSpeedMultiplier();
            cir.setReturnValue(baseSpeed * multiplier);
        }
    }

    @Inject(
            method = "getMaxDamage()I",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyDurabilityBasedOnQuality(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        int originalDurability = cir.getReturnValue();

        if (originalDurability <= 0) {
            return;
        }

        boolean blacklisted = ItemUtils.isDurabilityBlacklisted(stack);

        float baseMultiplier = ServerConfig.BASE_DURABILITY_MULTIPLIER.get().floatValue();
        int newBaseDurability = blacklisted ? originalDurability : (int) (originalDurability * baseMultiplier);

        // Apply quality multiplier
        if (stack.has(ModComponents.FORGING_QUALITY)) {
            ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY);
            if (quality != null) {
                float multiplier = quality.getDurabilityMultiplier();
                newBaseDurability = (int) (newBaseDurability * multiplier);
            }
        }

        // Apply durability reductions
        if (stack.has(ModComponents.REDUCED_GRIND_COUNT)) {
            Integer reductions = stack.get(ModComponents.REDUCED_GRIND_COUNT.get());
            if (reductions != null) {
                float durabilityPenaltyMultiplier = 1.0f - (reductions * ServerConfig.DURABILITY_REDUCE_PER_GRIND.get().floatValue());
                durabilityPenaltyMultiplier = Math.max(0.1f, durabilityPenaltyMultiplier);
                newBaseDurability = (int) (newBaseDurability * durabilityPenaltyMultiplier);
            }
        }

        cir.setReturnValue(newBaseDurability);
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void fixDurabilityBar(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageableItem()) return;

        int maxDamage = stack.getMaxDamage(); // includes quality/durability changes
        int damage = stack.getDamageValue();

        // Clamp to valid range
        if (damage >= maxDamage) {
            cir.setReturnValue(0);
            return;
        }

        int width = Math.round(13.0F - (float) damage * 13.0F / (float) maxDamage);
        cir.setReturnValue(width);
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void fixDurabilityBarColor(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageableItem()) return;

        int max = stack.getMaxDamage(); // Includes quality/durability changes
        int damage = stack.getDamageValue();

        if (max <= 0) {
            cir.setReturnValue(0xFFFFFF); // fallback white
            return;
        }

        float ratio = Math.max(0.0F, 1.0F - (float) damage / (float) max);

        // Vanilla bar color: hue from red (0.0) to green (0.333...)
        float hue = ratio / 3.0F; // [0, 0.33]

        int color = Mth.hsvToRgb(hue, 1.0F, 1.0F);

        cir.setReturnValue(color);
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void qualityBasedBreak(int amount, ServerLevel level, LivingEntity entity, Consumer<Item> itemConsumer, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        int currentDamage = stack.getDamageValue();
        int newDamage = currentDamage + amount;
        int max = stack.getMaxDamage();

        if (currentDamage < max && newDamage >= max) {
            if (!ServerConfig.ENABLE_QUALITY_BREAK_SYSTEM.get()) {
                return;
            }

            float breakChance = getBreakChance(stack);

            if (entity.getRandom().nextFloat() < breakChance) {
                return;
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
            } else {
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

            ci.cancel();
        }
    }

    @Unique
    private float getBreakChance(ItemStack stack) {
        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY);
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

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void disableUseOn(net.minecraft.world.item.context.UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (isBroken(stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void disableUse(Level level, Player player, InteractionHand hand,
                            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (isBroken(stack)) {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
        }
    }


}
