package net.stirdrem.overgeared.mixin;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ForgingQualityHelper;
import net.stirdrem.overgeared.util.ItemUtils;
import net.stirdrem.overgeared.util.ModTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(
            method = "getDestroySpeed",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        float baseSpeed = cir.getReturnValueF();

        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY);
        if (quality != null) {
            float multiplier = quality.getDamageMultiplier();
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
            float multiplier = ForgingQualityHelper.getQualityMultiplier(stack);
            newBaseDurability = (int) (newBaseDurability * multiplier);
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
}
