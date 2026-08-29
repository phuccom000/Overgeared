package net.stirdrem.overgeared.compat.accessories;

import com.google.common.collect.Multimap;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.attributes.AttributeModificationData;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.accessories.utils.AttributeUtils;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;
import net.stirdrem.overgeared.event.ModEvents;

import java.util.Collection;
import java.util.List;

import static net.stirdrem.overgeared.event.ModEvents.createModifiedAttribute;
import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

/**
 * Applies the mod's quality-based attribute bonuses to items worn in Accessories slots,
 * mirroring what ItemStackAttributeMixin already does for regular equipment slots.
 */
public class AttributeModifierHandler implements AdjustAttributeModifierCallback {

    public static void register() {
        AdjustAttributeModifierCallback.EVENT.register(new AttributeModifierHandler());
    }

    @Override
    public void adjustAttributes(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        if (isBroken(stack)) return;
        if (!stack.hasNbt()) return;

        String quality = stack.getNbt().getString("ForgingQuality");
        if (quality.isEmpty()) return;

        for (QualityAttributeDefinition def : QualityAttributeReloadListener.INSTANCE.getAll()) {
            if (!ModEvents.matches(stack, def.targets())) continue;

            QualityValue value = def.qualities().get(quality);
            if (value == null || value.amount() == 0) continue;

            EntityAttribute attribute = Registries.ATTRIBUTE.get(def.attribute());
            if (attribute == null) continue;

            modifyAttribute(builder, attribute, value.amount(), value.operation(), quality);
        }
    }

    private void modifyAttribute(AccessoryAttributeBuilder builder, EntityAttribute attribute, double bonus, EntityAttributeModifier.Operation operation, String quality) {
        if (bonus == 0) return;

        Multimap<EntityAttribute, EntityAttributeModifier> originalModifiers = builder.getAttributeModifiers(false);
        if (!originalModifiers.containsKey(attribute)) return;

        List<EntityAttributeModifier> modifiers = List.copyOf(originalModifiers.get(attribute));

        for (EntityAttributeModifier modifier : modifiers) {
            Identifier location = AttributeUtils.getLocation(modifier.getName());

            AttributeModificationData exclusiveData = builder.getExclusive(attribute, location);

            if (exclusiveData != null) {
                if (operation == EntityAttributeModifier.Operation.ADDITION) builder.removeExclusive(attribute, location);

                EntityAttributeModifier modified = createModifiedAttribute(modifier, bonus, operation, quality);

                builder.addExclusive(attribute, modified);
            } else {
                Collection<AttributeModificationData> stackableData = builder.getStacks(attribute, location);

                if (!stackableData.isEmpty()) {
                    if (operation == EntityAttributeModifier.Operation.ADDITION) builder.removeStacks(attribute, location);

                    for (AttributeModificationData data : stackableData) {
                        EntityAttributeModifier originalStackMod = data.modifier();
                        EntityAttributeModifier modifiedStack = createModifiedAttribute(originalStackMod, bonus, operation, quality);

                        builder.addStackable(attribute, location, modifiedStack.getValue(), modifiedStack.getOperation());
                    }
                }
            }
        }
    }
}
