package net.stirdrem.overgeared.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.heateditem.HeatedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ContainerCoolingMixin {

    @Unique
    private AbstractContainerMenu overgeared$lastMenu = null;

    @Inject(method = "doTick", at = @At("HEAD"))
    private void tickContainerCooling(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        AbstractContainerMenu menu = player.containerMenu;

        boolean justOpened = (menu != player.inventoryMenu && overgeared$lastMenu != menu);
        overgeared$lastMenu = menu;

        // No external container open — inventoryTick handles the player's own inventory
        if (menu == player.inventoryMenu) return;

        Level level = player.level();
        if (level.isClientSide) return;

        for (Slot slot : menu.slots) {
            if (HeatedItem.isHeated(slot.getItem())) {
                HeatedItem.handleCoolingContainer(slot, level, justOpened);
            }
        }
    }
}
