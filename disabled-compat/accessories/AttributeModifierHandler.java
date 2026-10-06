package net.stirdrem.overgeared.compat.accessories;

import com.google.common.collect.Multimap;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.attributes.AttributeModificationData;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.accessories.utils.AttributeUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
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
        if (!stack.hasTag()) return;

        String quality = stack.getTag().getString("ForgingQuality");
        if (quality.isEmpty()) return;

        for (QualityAttributeDefinition def : QualityAttributeReloadListener.INSTANCE.getAll()) {
            if (!ModEvents.matches(stack, def.targets())) continue;

            QualityValue value = def.qualities().get(quality);
            if (value == null || value.amount() == 0) continue;

            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(def.attribute());
            if (attribute == null) continue;

            modifyAttribute(builder, attribute, value.amount(), value.operation(), quality);
        }
    }

    private void modifyAttribute(AccessoryAttributeBuilder builder, Attribute attribute, double bonus, AttributeModifier.Operation operation, String quality) {
        if (bonus == 0) return;

        Multimap<Attribute, AttributeModifier> originalModifiers = builder.getAttributeModifiers(false);
        if (!originalModifiers.containsKey(attribute)) return;

        List<AttributeModifier> modifiers = List.copyOf(originalModifiers.get(attribute));

        for (AttributeModifier modifier : modifiers) {
            ResourceLocation location = AttributeUtils.getLocation(modifier.getName());

            AttributeModificationData exclusiveData = builder.getExclusive(attribute, location);

            if (exclusiveData != null) {
                if (operation == AttributeModifier.Operation.ADDITION) builder.removeExclusive(attribute, location);

                AttributeModifier modified = createModifiedAttribute(modifier, bonus, operation, quality);

                builder.addExclusive(attribute, modified);
            } else {
                Collection<AttributeModificationData> stackableData = builder.getStacks(attribute, location);

                if (!stackableData.isEmpty()) {
                    if (operation == AttributeModifier.Operation.ADDITION) builder.removeStacks(attribute, location);

                    for (AttributeModificationData data : stackableData) {
                        AttributeModifier originalStackMod = data.modifier();
                        AttributeModifier modifiedStack = createModifiedAttribute(originalStackMod, bonus, operation, quality);

                        builder.addStackable(attribute, location, modifiedStack.getAmount(), modifiedStack.getOperation());
                    }
                }
            }
        }
    }
}
