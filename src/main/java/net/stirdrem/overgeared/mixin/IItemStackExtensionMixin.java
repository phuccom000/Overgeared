package net.stirdrem.overgeared.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IItemStackExtension.class)
public interface IItemStackExtensionMixin {

    @Inject(method = "getAttributeModifiers", at = @At("HEAD"), cancellable = true, remap = false)
    private void brokenToolAttributes(CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        // Only modify if the tool is broken
        if (!stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage()) {
            return;
        }

        // Get the original attribute modifiers
        ItemAttributeModifiers originalModifiers = stack.getItem().getDefaultAttributeModifiers();

        // Create new builder with only attack speed kept
        var builder = ItemAttributeModifiers.builder();

        // Copy only attack speed attributes
        originalModifiers.modifiers().forEach(entry -> {
            if (entry.attribute() == net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        });

        cir.setReturnValue(builder.build());
    }
}
