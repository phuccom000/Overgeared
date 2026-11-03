package net.stirdrem.overgeared.neoforge.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.util.QualityBreak;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * NeoForge patches {@code hurtAndBreak} to take any {@link LivingEntity}.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackBreakMixin {
    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"), cancellable = true)
    private void qualityBasedBreak(int amount, ServerLevel level, LivingEntity entity, Consumer<Item> itemConsumer, CallbackInfo ci) {
        if (QualityBreak.preventBreak((ItemStack) (Object) this, amount, level, entity)) {
            ci.cancel();
        }
    }
}
