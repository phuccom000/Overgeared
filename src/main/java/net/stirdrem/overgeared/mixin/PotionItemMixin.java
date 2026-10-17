package net.stirdrem.overgeared.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

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
            method = "finishUsingItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/alchemy/PotionUtils;getMobEffects(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"
            ),
            cancellable = true
    )
    private void overgeared$onFinishUsing(ItemStack stack, Level world, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        CompoundTag tag = stack.getTag();
        if (stack.isEmpty() || tag == null || !tag.contains(TIPPED_USED_TAG)) {
            return;
        }

        Player player = entity instanceof Player ? (Player) entity : null;
        int tippedUsed = tag.getInt(TIPPED_USED_TAG);
        float scale = overgeared$calculateDurationScale(tippedUsed);

        if (!world.isClientSide()) {
            for (MobEffectInstance effect : PotionUtils.getMobEffects(stack)) {
                if (effect.getEffect().isInstantenous()) {
                    effect.getEffect().applyInstantenousEffect(player, player, entity, effect.getAmplifier(), 1.0D);
                } else {
                    entity.addEffect(overgeared$createScaledEffect(effect, scale));
                }
            }
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get((PotionItem) (Object) this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        ItemStack resultStack = stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
        if (player != null && !player.getAbilities().instabuild && stack.isEmpty()) {
            player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
        }

        entity.gameEvent(GameEvent.DRINK);
        cir.setReturnValue(resultStack);
    }

    @ModifyArg(
            method = "appendHoverText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/alchemy/PotionUtils;addPotionTooltip(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;F)V"
            ),
            index = 2
    )
    private float overgeared$modifyTooltipDurationScale(ItemStack stack, List<Component> tooltip, float originalScale) {
        CompoundTag tag = stack.getTag();
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
    private static MobEffectInstance overgeared$createScaledEffect(MobEffectInstance original, float scale) {
        return new MobEffectInstance(
                original.getEffect(),
                Math.max(1, (int) (original.getDuration() * scale)),
                original.getAmplifier(),
                original.isAmbient(),
                original.isVisible(),
                original.showIcon()
        );
    }
}
