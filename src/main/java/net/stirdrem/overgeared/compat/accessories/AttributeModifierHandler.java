package net.stirdrem.overgeared.compat.accessories;

import com.google.common.collect.Multimap;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.attributes.AttributeModificationData;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.accessories.utils.AttributeUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

// Register to modify attributes dynamically
public class AttributeModifierHandler implements AdjustAttributeModifierCallback {

    public static void register() {
        AdjustAttributeModifierCallback.EVENT.register(new AttributeModifierHandler());
    }

    @Override
    public void adjustAttributes(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        if (!stack.hasTag()) return;

        String quality = stack.getTag().getString("ForgingQuality");
        if (quality.isEmpty()) return;

        for (QualityAttributeDefinition def :
                QualityAttributeReloadListener.INSTANCE.getAll()) {

            if (!matches(stack, def.targets())) continue;

            QualityValue value = def.qualities().get(quality);
            if (value == null || value.amount() == 0) continue;

            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(def.attribute());
            if (attribute == null) continue;

            modifyAttribute(builder, attribute, value.amount(), value.operation());
        }
    }

    private static boolean matches(ItemStack stack, List<QualityTarget> targets) {
        Item item = stack.getItem();

        for (QualityTarget target : targets) {
            switch (target.type()) {
                case ITEM -> {
                    ResourceLocation itemId =
                            ForgeRegistries.ITEMS.getKey(item);
                    if (itemId != null && itemId.equals(target.id())) {
                        return true;
                    }
                }

                case ITEM_TAG -> {
                    if (target.id() == null) break;

                    TagKey<Item> tag = TagKey.create(
                            Registries.ITEM,
                            target.id()
                    );

                    if (stack.is(tag)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void modifyAttribute(AccessoryAttributeBuilder builder, Attribute attribute, double bonus, AttributeModifier.Operation operation) {
        if (bonus == 0) return;

        // Get all existing modifiers for this attribute
        Multimap<Attribute, AttributeModifier> originalModifiers = builder.getAttributeModifiers(false);

        if (!originalModifiers.containsKey(attribute)) return;

        List<AttributeModifier> modifiers = List.copyOf(originalModifiers.get(attribute));

        for (AttributeModifier modifier : modifiers) {
            if (modifier.getAmount() == 0) continue;

            // Get the ResourceLocation for this modifier
            ResourceLocation location = AttributeUtils.getLocation(modifier.getName());

            // Check if it's an exclusive or stackable modifier
            AttributeModificationData exclusiveData = builder.getExclusive(attribute, location);

            if (exclusiveData != null) {
                // It's an exclusive modifier - remove and replace
                builder.removeExclusive(attribute, location);

                // Create modified attribute
                AttributeModifier modified = createModifiedAttribute(modifier, bonus, operation);

                // Add it back as exclusive
                builder.addExclusive(attribute, modified);
            } else {
                // It's a stackable modifier - get all stackable modifiers with this location
                Collection<AttributeModificationData> stackableData = builder.getStacks(attribute, location);

                if (!stackableData.isEmpty()) {
                    // Remove all stackable modifiers with this location
                    builder.removeStacks(attribute, location);

                    // Add modified versions
                    for (AttributeModificationData data : stackableData) {
                        AttributeModifier originalStackMod = data.modifier();
                        AttributeModifier modifiedStack = createModifiedAttribute(originalStackMod, bonus, operation);

                        // Add as stackable with same location
                        builder.addStackable(attribute, location, modifiedStack.getAmount(), modifiedStack.getOperation());
                    }
                }
            }
        }
    }

    // Create modified attribute - exactly like your method
    private static AttributeModifier createModifiedAttribute(AttributeModifier original, double bonus, AttributeModifier.Operation operation) {
        return new AttributeModifier(
                original.getId(),
                "Overgeared",  // Your custom name
                original.getAmount() + bonus,  // Add bonus instead of multiply
                operation
        );
    }

    // Create a unique ResourceLocation for the modifier
    private ResourceLocation createModifiedResourceLocation(AttributeModifier modifier) {
        // Create a deterministic ID based on the original modifier
        String baseName = modifier.getName().toLowerCase()
                .replaceAll("[^a-z0-9/._-]", "")
                .replace(" ", "_");

        return ResourceLocation.fromNamespaceAndPath(
                OvergearedMod.MOD_ID,
                "overgeared_" + baseName + "_" + Math.abs(modifier.getId().hashCode())
        );
    }

}