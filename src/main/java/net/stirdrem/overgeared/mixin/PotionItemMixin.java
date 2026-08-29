package net.stirdrem.overgeared.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * A potion that has been used to tip arrows by hand (see ModItemInteractEvents' "TippedUsed"
 * writes) has weaker effects the more it's been tipped from - this scales the drunk effect
 * durations and the tooltip down to match, based on the "TippedUsed" count.
 */
@Mixin(PotionItem.class)
public abstract class PotionItemMixin {

    @Unique
    private static final String TIPPED_USED_TAG = "TippedUsed";

    @Unique
    private static final float MIN_DURATION_SCALE = 0.1f;

    @Inject(
            method = "finishUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/potion/PotionUtil;getPotionEffects(Lnet/minecraft/item/ItemStack;)Ljava/util/List;"
            ),
            cancellable = true
    )
    private void overgeared$onFinishUsing(ItemStack stack, World world, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        NbtCompound tag = stack.getNbt();
        if (stack.isEmpty() || tag == null || !tag.contains(TIPPED_USED_TAG)) {
            return;
        }

        PlayerEntity player = entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
        int tippedUsed = tag.getInt(TIPPED_USED_TAG);
        float scale = overgeared$calculateDurationScale(tippedUsed);

        if (!world.isClient()) {
            for (StatusEffectInstance effect : PotionUtil.getPotionEffects(stack)) {
                if (effect.getEffectType().isInstant()) {
                    effect.getEffectType().applyInstantEffect(player, player, entity, effect.getAmplifier(), 1.0D);
                } else {
                    entity.addStatusEffect(overgeared$createScaledEffect(effect, scale));
                }
            }
        }

        if (player != null) {
            player.incrementStat(Stats.USED.getOrCreateStat((PotionItem) (Object) this));
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        ItemStack resultStack = stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
        if (player != null && !player.getAbilities().creativeMode && stack.isEmpty()) {
            player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
        }

        entity.emitGameEvent(GameEvent.DRINK);
        cir.setReturnValue(resultStack);
    }

    @ModifyArg(
            method = "appendTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/potion/PotionUtil;buildTooltip(Lnet/minecraft/item/ItemStack;Ljava/util/List;F)V"
            ),
            index = 2
    )
    private float overgeared$modifyTooltipDurationScale(ItemStack stack, List<Text> tooltip, float originalScale) {
        NbtCompound tag = stack.getNbt();
        if (tag != null && tag.contains(TIPPED_USED_TAG)) {
            return overgeared$calculateDurationScale(tag.getInt(TIPPED_USED_TAG));
        }
        return originalScale;
    }

    @Unique
    private static float overgeared$calculateDurationScale(int tippedUsed) {
        return Math.max(MIN_DURATION_SCALE, 1.0f - (tippedUsed / 8.0f));
    }

    @Unique
    private static StatusEffectInstance overgeared$createScaledEffect(StatusEffectInstance original, float scale) {
        return new StatusEffectInstance(
                original.getEffectType(),
                Math.max(1, (int) (original.getDuration() * scale)),
                original.getAmplifier(),
                original.isAmbient(),
                original.shouldShowParticles(),
                original.shouldShowIcon()
        );
    }
}
