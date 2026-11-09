package net.stirdrem.overgeared.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.util.ModTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to handle HEATED_TIME during item stack merging in slots.
 * When heated items merge, the newest timestamp (most time remaining) is preserved.
 */
@Mixin(Slot.class)
public abstract class SlotMixin {

    @Shadow public abstract ItemStack getItem();
    @Shadow public abstract void set(ItemStack stack);
    @Shadow public Container container;

    /**
     * Thread-local storage to capture the inserting stack's heated time BEFORE the merge.
     * This is needed because after merge, the inserting stack may be empty.
     */
    @Unique
    private static final ThreadLocal<Long> overgeared$capturedInsertingTime = new ThreadLocal<>();

    /**
     * Capture the inserting stack's HEATED_TIME BEFORE the merge happens.
     */
    @Inject(method = "safeInsert(Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"))
    private void overgeared$captureHeatedTimeBeforeMerge(ItemStack insertingStack, int count, CallbackInfoReturnable<ItemStack> cir) {
        overgeared$capturedInsertingTime.remove();
        
        if (insertingStack.isEmpty()) return;
        
        // Check if the inserting stack is heated
        boolean insertingHeated = insertingStack.is(ModTags.Items.HEATED_METALS) 
            || Boolean.TRUE.equals(insertingStack.get(ModComponents.HEATED_COMPONENT.get()));
        
        if (insertingHeated) {
            Long insertingTime = insertingStack.get(ModComponents.HEATED_TIME.get());
            if (insertingTime != null) {
                overgeared$capturedInsertingTime.set(insertingTime);
            }
        }
    }

    /**
     * After the merge, update HEATED_TIME to the newest value if applicable.
     */
    @Inject(method = "safeInsert(Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private void overgeared$preserveNewestHeatedTime(ItemStack insertingStack, int count, CallbackInfoReturnable<ItemStack> cir) {
        Long capturedTime = overgeared$capturedInsertingTime.get();
        overgeared$capturedInsertingTime.remove();
        
        if (capturedTime == null) return;
        
        ItemStack slotStack = this.getItem();
        if (slotStack.isEmpty()) return;
        
        // Check if slot stack is heated
        boolean slotHeated = slotStack.is(ModTags.Items.HEATED_METALS) 
            || Boolean.TRUE.equals(slotStack.get(ModComponents.HEATED_COMPONENT.get()));
        
        if (!slotHeated) return;
        
        // Get current slot timestamp
        Long slotTime = slotStack.get(ModComponents.HEATED_TIME.get());
        
        // If the captured (inserting) time is newer, update the slot's timestamp
        if (slotTime == null || capturedTime > slotTime) {
            slotStack.set(ModComponents.HEATED_TIME.get(), capturedTime);
            container.setChanged();
        }
    }
}
