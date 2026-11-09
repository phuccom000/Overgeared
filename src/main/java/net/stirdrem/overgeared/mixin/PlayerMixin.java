package net.stirdrem.overgeared.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

/**
 * Broken (quality break system) swords can't sweep-attack. 26.3 moved the sweep check (now a
 * {@code ItemTags.SWORDS} test) into {@code Player#isSweepAttack(boolean, boolean, boolean)}.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "isSweepAttack(ZZZ)Z", at = @At("RETURN"), cancellable = true)
    private void overgeared$disableSweepWhenBroken(boolean fullStrengthAttack, boolean criticalAttack, boolean knockbackAttack,
                                                   CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && isBroken(((Player) (Object) this).getMainHandItem())) {
            cir.setReturnValue(false);
        }
    }
}
