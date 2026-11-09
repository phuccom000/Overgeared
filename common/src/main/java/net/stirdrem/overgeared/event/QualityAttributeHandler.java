package net.stirdrem.overgeared.event;
import net.stirdrem.overgeared.util.ItemKinds;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;

import java.util.ArrayList;
import java.util.List;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

public class QualityAttributeHandler {

    /**
     * Loader-independent view of an item's attribute modifiers while they are being computed.
     */
    public interface ModifierEditor {
        List<ItemAttributeModifiers.Entry> getModifiers();

        void removeModifier(Holder<Attribute> attribute, Identifier id);

        void addModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot);
    }

    /**
     * Applies the forging-quality bonuses to the item's modifiers.
     */
    public static void applyQualityModifiers(ItemStack stack, ModifierEditor event) {
        if (isBroken(stack)) return;

        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY.get());
        if (quality != null && quality != ForgingQuality.NONE)
            for (QualityAttributeDefinition def : QualityAttributeReloadListener.getAll()) {

                if (!matches(stack, def.targets()))
                    continue;

                QualityValue value = def.qualities().get(quality.getDisplayName());
                if (value == null || value.amount() == 0)
                    continue;
                Holder<Attribute> attribute = BuiltInRegistries.ATTRIBUTE
                        .get(def.attribute()).orElse(null);
                if (attribute == null)
                    continue;

                modifyAttribute(event, attribute, value.amount(), value.operation(), quality);
            }
    }

    /**
     * Builds a modified copy of an item's modifiers. Used where the loader has no event for it.
     */
    public static ItemAttributeModifiers applyQualityModifiers(ItemStack stack, ItemAttributeModifiers modifiers) {
        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY.get());
        if (quality == null || quality == ForgingQuality.NONE || isBroken(stack)) {
            return modifiers;
        }
        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(modifiers.modifiers());
        applyQualityModifiers(stack, new ModifierEditor() {
            @Override
            public List<ItemAttributeModifiers.Entry> getModifiers() {
                return List.copyOf(entries);
            }

            @Override
            public void removeModifier(Holder<Attribute> attribute, Identifier id) {
                entries.removeIf(entry -> entry.attribute().equals(attribute) && entry.modifier().id().equals(id));
            }

            @Override
            public void addModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
                // Same rule as NeoForge: a modifier is not added again if one with the same id exists
                boolean present = entries.stream()
                        .anyMatch(entry -> entry.attribute().equals(attribute) && entry.modifier().id().equals(modifier.id()));
                if (!present) {
                    entries.add(new ItemAttributeModifiers.Entry(attribute, modifier, slot));
                }
            }
        });
        return new ItemAttributeModifiers(entries);
    }

    public static boolean matches(ItemStack stack, List<QualityTarget> targets) {
        Item item = stack.getItem();

        for (QualityTarget target : targets) {
            switch (target.type()) {
                case WEAPON -> {
                    if (ItemKinds.isTiered(item) ||
                            item instanceof ProjectileWeaponItem) {
                        return true;
                    }
                }

                case ARMOR -> {
                    if (ItemKinds.isArmor(item)) {
                        return true;
                    }
                }

                case ITEM -> {
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
                    if (itemId != null && itemId.equals(target.id())) {
                        return true;
                    }
                }

                case ITEM_TAG -> {
                    if (target.id() == null)
                        break;

                    TagKey<Item> tag = TagKey.create(
                            Registries.ITEM,
                            target.id());

                    if (stack.is(tag)) {
                        return true;
                    }
                }
                case ITEM_ALL -> {
                    return true;
                }
            }
        }
        return false;
    }

    private static void modifyAttribute(ModifierEditor event, Holder<Attribute> attribute, double bonus,
                                        AttributeModifier.Operation operation, ForgingQuality quality) {
        // Get all modifiers currently on the item
        var modifiers = event.getModifiers();

        // Filter for the target attribute
        var targetModifiers = new ArrayList<>(modifiers.stream()
                .filter(entry -> entry.attribute().equals(attribute))
                .toList());

        // Modify each one
        for (var entry : targetModifiers) {
            AttributeModifier originalModifier = entry.modifier();

            // Remove original
            if (operation == AttributeModifier.Operation.ADD_VALUE)
                event.removeModifier(entry.attribute(), originalModifier.id());

            // Add modified version
            event.addModifier(
                    entry.attribute(),
                    createModifiedAttribute(originalModifier, bonus, operation, quality),
                    entry.slot());
        }
    }

    private static AttributeModifier createModifiedAttribute(AttributeModifier original, double bonus,
                                                             AttributeModifier.Operation operation, ForgingQuality quality) {
        Identifier id;
        double amount;
        if (operation == AttributeModifier.Operation.ADD_VALUE) {
            id = original.id();
            amount = original.amount() + bonus;
        } else {
            id = Identifier.parse(original.id() + "_overgeared_" + quality.getDisplayName() + "_"
                    + operation.name().toLowerCase(java.util.Locale.ROOT) + (int) bonus * 10);
            amount = bonus;
        }
        return new AttributeModifier(id, amount, operation);
    }
}