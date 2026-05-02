package net.stirdrem.overgeared.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

@Mixin(Player.class)
public abstract class PlayerMixin {

    /**
     * Redirect the canPerformAction call to check for broken swords
     */
    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;canPerformAction(Lnet/neoforged/neoforge/common/ItemAbility;)Z"
            )
    )
    private boolean redirectCanPerformAction(ItemStack itemStack, ItemAbility ability) {
        if (ability == ItemAbilities.SWORD_SWEEP) {
            if (isBroken(itemStack)) {
                return false;
            }
        }
        return itemStack.canPerformAction(ability);
    }
}