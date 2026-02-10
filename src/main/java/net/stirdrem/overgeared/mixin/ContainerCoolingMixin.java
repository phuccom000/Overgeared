package net.stirdrem.overgeared.mixin;

import java.util.List;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.stirdrem.overgeared.item.custom.HeatedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerListener;

@Mixin(AbstractContainerMenu.class)
public class ContainerCoolingMixin {

    @Shadow @Final private List<ContainerListener> containerListeners;

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void tickOpenContainer(CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        Level level = null;

        for (Slot slot : menu.slots) {
            if (slot.container instanceof BlockEntity be) {
                level = be.getLevel();
                if (level != null && !level.isClientSide) break;
            }
        }
        if (level == null || level.isClientSide) return;

        for (Slot slot : menu.slots) {
            if (slot.getItem().getItem() instanceof HeatedItem) {
                HeatedItem.handleCoolingContainer(slot, level);
            }
        }
    }
}