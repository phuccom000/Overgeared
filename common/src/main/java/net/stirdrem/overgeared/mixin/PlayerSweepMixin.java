package net.stirdrem.overgeared.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

/**
 * Broken swords cannot sweep. Vanilla decides in {@code Player#isSweepAttack} (main hand in
 * {@code #minecraft:swords}) on both loaders.
 */
@Mixin(Player.class)
public abstract class PlayerSweepMixin {
    @Inject(method = "isSweepAttack", at = @At("RETURN"), cancellable = true)
    private void overgeared$noSweepWhenBroken(boolean fullStrengthAttack, boolean criticalAttack, boolean knockbackAttack,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && isBroken(((Player) (Object) this).getMainHandItem())) {
            cir.setReturnValue(false);
        }
    }
}
