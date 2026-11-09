package net.stirdrem.overgeared.fabric.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

/**
 * Broken swords cannot sweep. Vanilla checks {@code instanceof SwordItem} where NeoForge
 * checks {@code ItemAbilities.SWORD_SWEEP} (see the NeoForge {@code PlayerMixin}).
 * <p>
 * Uses a MixinExtras expression with a class literal rather than {@code args = "classValue=..."}: without a refmap,
 * Loom remaps class literals but not {@code args} strings, so the latter never matches in production.
 */
@Mixin(Player.class)
public abstract class PlayerSweepMixin {
    @Definition(id = "SwordItem", type = SwordItem.class)
    @Expression("? instanceof SwordItem")
    @ModifyExpressionValue(method = "attack", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean overgeared$noSweepWhenBroken(boolean isSword) {
        return isSword && !isBroken(((Player) (Object) this).getMainHandItem());
    }
}
