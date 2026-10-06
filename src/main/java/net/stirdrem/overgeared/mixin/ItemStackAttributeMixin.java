package net.stirdrem.overgeared.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.stirdrem.overgeared.event.ModEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Forge's ItemAttributeModifierEvent has no Fabric equivalent. In 26.3 every attribute-modifier
 * consumer (equipment attribute application and the tooltip) goes through the two
 * {@code ItemStack#forEachModifier} overloads, which read the ATTRIBUTE_MODIFIERS component; this
 * mixin swaps that component value for one with the quality bonus (quality_attributes datapack)
 * applied. Broken tools keep only their attack-speed modifiers, like the 1.20 port.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackAttributeMixin {

    @ModifyExpressionValue(
            method = {
                    "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V",
                    "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"
            },
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object overgeared$applyQualityAttributes(Object original) {
        if (!(original instanceof ItemAttributeModifiers modifiers)) return original;
        ItemStack self = (ItemStack) (Object) this;

        if (overgeared$isBrokenTool(self)) {
            return new ItemAttributeModifiers(modifiers.modifiers().stream()
                    .filter(e -> e.attribute().value() == Attributes.ATTACK_SPEED.value())
                    .toList());
        }
        return ModEvents.applyQualityAttributeModifiers(self, modifiers);
    }

    @Unique
    private static boolean overgeared$isBrokenTool(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage();
    }
}
