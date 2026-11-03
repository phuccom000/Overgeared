package net.stirdrem.overgeared.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

/**
 * Broken swords cannot sweep. Vanilla checks {@code instanceof SwordItem} where NeoForge
 * checks {@code ItemAbilities.SWORD_SWEEP} (see the NeoForge {@code PlayerMixin}).
 */
@Mixin(Player.class)
public abstract class PlayerSweepMixin {
    @ModifyExpressionValue(method = "attack", at = @At(value = "CONSTANT", args = "classValue=net/minecraft/world/item/SwordItem"))
    private boolean overgeared$noSweepWhenBroken(boolean isSword) {
        return isSword && !isBroken(((Player) (Object) this).getMainHandItem());
    }
}
