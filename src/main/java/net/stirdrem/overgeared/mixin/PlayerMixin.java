package net.stirdrem.overgeared.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;canPerformAction(Lnet/minecraftforge/common/ToolAction;)Z"
            )
    )
    private boolean disableSweepWhenBroken(ItemStack stack, net.minecraftforge.common.ToolAction action) {
        if (action == net.minecraftforge.common.ToolActions.SWORD_SWEEP) {
            if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage()) {
                return false;
            }
        }

        return stack.canPerformAction(action);
    }
}