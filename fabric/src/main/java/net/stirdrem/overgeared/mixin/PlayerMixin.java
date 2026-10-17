package net.stirdrem.overgeared.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

/**
 * Vanilla gates the sweep-attack purely on {@code itemStack.getItem() instanceof SwordItem} deep
 * inside PlayerEntity.attack() - there's no clean event/hook at that point, so this uses
 * MixinExtras' expression matching to intercept just that instanceof check rather than a fragile
 * local-variable-ordinal mixin into a huge, heavily-obfuscated vanilla method.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Definition(id = "sword", type = SwordItem.class)
    @Definition(id = "getItem", method = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;")
    @Expression("?.getItem() instanceof sword")
    @ModifyExpressionValue(method = "attack", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean overgeared$disableSweepWhenBroken(boolean original) {
        if (original) {
            ItemStack stack = ((Player) (Object) this).getMainHandItem();
            if (isBroken(stack)) {
                return false;
            }
        }
        return original;
    }
}
