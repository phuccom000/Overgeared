package net.stirdrem.overgeared.mixin;

import java.util.List;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.item.custom.HeatedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.inventory.ContainerListener;

@Mixin(AbstractContainerMenu.class)
public class ContainerCoolingMixin {

    @Shadow @Final private List<ContainerListener> containerListeners;

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void tickOpenContainer(CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            ItemStack stack = slot.getItem();

            if (stack.getItem() instanceof HeatedItem) {
                if (!this.containerListeners.isEmpty()) { 
                    for (ContainerListener listener : this.containerListeners) {
                        if (listener instanceof ServerPlayer player) {
                            Level level = player.level();
                            HeatedItem.handleCoolingContainer(slot, level); 
                            break;
                        }
                    }
                }
            }
        }
    }
}