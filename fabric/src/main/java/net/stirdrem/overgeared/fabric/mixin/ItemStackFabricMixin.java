package net.stirdrem.overgeared.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.stirdrem.overgeared.event.QualityAttributeHandler;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.util.BrokenHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

/**
 * Item stack hooks NeoForge provides through events and item extensions.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackFabricMixin {

    /**
     * NeoForge: {@code ItemAttributeModifierEvent} (forging quality) and the broken-tool
     * {@code IItemStackExtension#getAttributeModifiers} override.
     */
    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V",
            at = @At("HEAD"), cancellable = true)
    private void overgeared$modifiersForGroup(EquipmentSlotGroup group, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
        ItemAttributeModifiers modified = overgeared$modifiedAttributes();
        if (modified != null) {
            modified.forEach(group, action);
            EnchantmentHelper.forEachModifier((ItemStack) (Object) this, group, action);
            ci.cancel();
        }
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("HEAD"), cancellable = true)
    private void overgeared$modifiersForSlot(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
        ItemAttributeModifiers modified = overgeared$modifiedAttributes();
        if (modified != null) {
            modified.forEach(slot, action);
            EnchantmentHelper.forEachModifier((ItemStack) (Object) this, slot, action);
            ci.cancel();
        }
    }

    /**
     * @return the modifiers to use instead of vanilla's, or null to leave vanilla alone
     */
    @Unique
    @SuppressWarnings("deprecation")
    private ItemAttributeModifiers overgeared$modifiedAttributes() {
        ItemStack stack = (ItemStack) (Object) this;
        ItemAttributeModifiers base = stack.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (base.modifiers().isEmpty()) {
            base = stack.getItem().getDefaultAttributeModifiers();
        }

        if (BrokenHelper.isBroken(stack)) {
            // Broken tools keep only their attack speed
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            for (ItemAttributeModifiers.Entry entry : base.modifiers()) {
                if (entry.attribute().equals(Attributes.ATTACK_SPEED)) {
                    builder.add(entry.attribute(), entry.modifier(), entry.slot());
                }
            }
            return builder.build();
        }

        ItemAttributeModifiers modified = QualityAttributeHandler.applyQualityModifiers(stack, base);
        return modified == base ? null : modified;
    }

    /**
     * NeoForge: {@code IItemExtension#getMaxDamage(ItemStack)}, which tool casts override to read the config.
     */
    @ModifyExpressionValue(method = "getMaxDamage", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object overgeared$toolCastMaxDamage(Object original) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ToolCastItem toolCast) {
            return toolCast.getMaxDamage(stack);
        }
        return original;
    }
}
