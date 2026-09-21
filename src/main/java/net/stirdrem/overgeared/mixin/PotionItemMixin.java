package net.stirdrem.overgeared.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.stirdrem.overgeared.components.ModComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Consumer;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin {
    @Unique
    private static final float MIN_DURATION_SCALE = 0.1f;

    @WrapOperation(
            method = "finishUsingItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/alchemy/PotionContents;forEachEffect(Ljava/util/function/Consumer;)V"
            )
    )
    private void overgeared$scaleEffects(
            PotionContents contents,
            Consumer<MobEffectInstance> action,
            Operation<Void> original,
            ItemStack stack,
            net.minecraft.world.level.Level level,
            LivingEntity entity
    ) {
        Integer tippedUsed = stack.get(ModComponents.TIPPED_USES);

        if (tippedUsed == null) {
            original.call(contents, action);
            return;
        }

        float scale = overgeared$calculateDurationScale(tippedUsed);

        Consumer<MobEffectInstance> scaledAction =
                effect -> action.accept(overgeared$createScaledEffect(effect, scale));

        original.call(contents, scaledAction);
    }

    @WrapOperation(
            method = "appendHoverText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/alchemy/PotionContents;addPotionTooltip(Ljava/util/function/Consumer;FF)V"
            )
    )
    private void overgeared$scaleTooltip(
            PotionContents contents,
            Consumer<Component> tooltipAdder,
            float durationFactor,
            float ticksPerSecond,
            Operation<Void> original,
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        Integer tippedUsed = stack.get(ModComponents.TIPPED_USES);

        float scale = durationFactor;

        if (tippedUsed != null) {
            scale = overgeared$calculateDurationScale(tippedUsed);
        }

        original.call(contents, tooltipAdder, scale, ticksPerSecond);
    }

    @Unique
    private static float overgeared$calculateDurationScale(int tippedUsed) {
        return Math.max(MIN_DURATION_SCALE, 1.0f - (tippedUsed / 8.0f));
    }

    @Unique
    private static MobEffectInstance overgeared$createScaledEffect(
            MobEffectInstance original,
            float scale
    ) {
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