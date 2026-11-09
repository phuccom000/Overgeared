package net.stirdrem.overgeared.fabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.util.QualityBreak;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Vanilla {@code hurtAndBreak} takes a nullable {@link ServerPlayer} (NeoForge: any living entity).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackBreakMixin {
    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"), cancellable = true)
    private void qualityBasedBreak(int amount, ServerLevel level, ServerPlayer player, Consumer<Item> itemConsumer, CallbackInfo ci) {
        if (QualityBreak.preventBreak((ItemStack) (Object) this, amount, level, player)) {
            ci.cancel();
        }
    }
}
