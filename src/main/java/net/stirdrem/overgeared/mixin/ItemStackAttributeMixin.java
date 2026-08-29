package net.stirdrem.overgeared.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.stirdrem.overgeared.event.ModEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forge's ItemAttributeModifierEvent has no Fabric equivalent, so the quality attribute bonus
 * (defined via the quality_attributes datapack) is injected here instead. getAttributeModifiers
 * can return an immutable Multimap (Item.getAttributeModifiers' ImmutableMultimap fallback), so
 * a fresh mutable copy is built and returned rather than mutating the original in place.
 */
@Mixin(ItemStack.class)
public class ItemStackAttributeMixin {

    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
    private void overgeared$applyQualityAttributes(EquipmentSlot slot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        ItemStack self = (ItemStack) (Object) this;

        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create(cir.getReturnValue());
        ModEvents.applyQualityAttributeModifiers(self, modifiers);
        cir.setReturnValue(modifiers);
    }
}
