package net.stirdrem.overgeared.mixin;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.util.ModTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/**
 * Mixin to allow heated items to stack regardless of their HEATED_TIME value.
 * When comparing heated items for stacking, we ignore the HEATED_TIME component.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackStackingMixin {

    /**
     * Intercept stacking comparison to ignore HEATED_TIME for heated items.
     * This allows heated items with different cooling timestamps to stack together.
     */
    @Inject(method = "isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private static void overgeared$ignoreHeatedTimeForStacking(ItemStack stack, ItemStack other, CallbackInfoReturnable<Boolean> cir) {
        // Check if heated by tag OR by component
        boolean stackHeated = stack.is(ModTags.Items.HEATED_METALS) 
            || Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT.get()));
        boolean otherHeated = other.is(ModTags.Items.HEATED_METALS) 
            || Boolean.TRUE.equals(other.get(ModComponents.HEATED_COMPONENT.get()));
        
        if (!stackHeated || !otherHeated) {
            return; // Let normal comparison proceed
        }
        
        // Both are heated - compare everything EXCEPT HEATED_TIME
        if (!stack.is(other.getItem())) {
            cir.setReturnValue(false);
            return;
        }
        
        // Compare all components, ignoring HEATED_TIME
        DataComponentMap stackComponents = stack.getComponents();
        DataComponentMap otherComponents = other.getComponents();
        
        // Check all components in stack
        for (var entry : stackComponents) {
            DataComponentType<?> type = entry.type();
            
            // Skip HEATED_TIME comparison
            if (type == ModComponents.HEATED_TIME.get()) {
                continue;
            }
            
            Object stackValue = entry.value();
            Object otherValue = otherComponents.get(type);
            
            if (!Objects.equals(stackValue, otherValue)) {
                cir.setReturnValue(false);
                return;
            }
        }
        
        // Check if other has components that stack doesn't (excluding HEATED_TIME)
        for (var entry : otherComponents) {
            DataComponentType<?> type = entry.type();
            
            if (type == ModComponents.HEATED_TIME.get()) {
                continue;
            }
            
            if (!stackComponents.has(type)) {
                cir.setReturnValue(false);
                return;
            }
        }
        
        // All non-HEATED_TIME components match - allow stacking
        cir.setReturnValue(true);
    }
}
