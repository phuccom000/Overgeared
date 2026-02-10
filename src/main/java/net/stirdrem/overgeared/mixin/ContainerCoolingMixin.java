package net.stirdrem.overgeared.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.item.custom.HeatedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class ContainerCoolingMixin {

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void tickOpenContainer(CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            ItemStack stack = slot.getItem();

            if (stack.getItem() instanceof HeatedItem heatedItem) {
                if (!menu.remoteSlots.isEmpty()) { 
                    heatedItem.handleCoolingContainer(stack, menu.slots.get(0).container.getLevel()); 
                }
            }
        }
    }
}