package net.stirdrem.overgeared.compat.accessories;

import com.google.common.collect.Multimap;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.attributes.AttributeModificationData;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.accessories.utils.AttributeUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;
import net.stirdrem.overgeared.event.ModEvents;

import java.util.Collection;
import java.util.List;

import static net.stirdrem.overgeared.event.ModEvents.createModifiedAttribute;

// Register to modify attributes dynamically
public class AttributeModifierHandler implements AdjustAttributeModifierCallback {

    public static void register() {
        AdjustAttributeModifierCallback.EVENT.register(new AttributeModifierHandler());
    }

    @Override
    public void adjustAttributes(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage()) {
            return;
        }
        if (!stack.hasTag()) return;

        String quality = stack.getTag().getString("ForgingQuality");
        if (quality.isEmpty()) return;

        for (QualityAttributeDefinition def :
                QualityAttributeReloadListener.INSTANCE.getAll()) {

            if (!ModEvents.matches(stack, def.targets())) continue;

            QualityValue value = def.qualities().get(quality);
            if (value == null || value.amount() == 0) continue;

            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(def.attribute());
            if (attribute == null) continue;

            modifyAttribute(builder, attribute, value.amount(), value.operation(), quality);
        }
    }

    private void modifyAttribute(AccessoryAttributeBuilder builder, Attribute attribute, double bonus, AttributeModifier.Operation operation, String quality) {
        if (bonus == 0) return;

        // Get all existing modifiers for this attribute
        Multimap<Attribute, AttributeModifier> originalModifiers = builder.getAttributeModifiers(false);

        if (!originalModifiers.containsKey(attribute)) return;

        List<AttributeModifier> modifiers = List.copyOf(originalModifiers.get(attribute));

        for (AttributeModifier modifier : modifiers) {
            //if (modifier.getAmount() == 0) continue;

            // Get the ResourceLocation for this modifier
            ResourceLocation location = AttributeUtils.getLocation(modifier.getName());

            // Check if it's an exclusive or stackable modifier
            AttributeModificationData exclusiveData = builder.getExclusive(attribute, location);

            if (exclusiveData != null) {
                // It's an exclusive modifier - remove and replace
                if (operation == AttributeModifier.Operation.ADDITION) builder.removeExclusive(attribute, location);

                // Create modified attribute
                AttributeModifier modified = createModifiedAttribute(modifier, bonus, operation, quality);

                // Add it back as exclusive
                builder.addExclusive(attribute, modified);
            } else {
                // It's a stackable modifier - get all stackable modifiers with this location
                Collection<AttributeModificationData> stackableData = builder.getStacks(attribute, location);

                if (!stackableData.isEmpty()) {
                    // Remove all stackable modifiers with this location
                    if (operation == AttributeModifier.Operation.ADDITION) builder.removeStacks(attribute, location);

                    // Add modified versions
                    for (AttributeModificationData data : stackableData) {
                        AttributeModifier originalStackMod = data.modifier();
                        AttributeModifier modifiedStack = createModifiedAttribute(originalStackMod, bonus, operation, quality);

                        // Add as stackable with same location
                        builder.addStackable(attribute, location, modifiedStack.getAmount(), modifiedStack.getOperation());
                    }
                }
            }
        }
    }

}