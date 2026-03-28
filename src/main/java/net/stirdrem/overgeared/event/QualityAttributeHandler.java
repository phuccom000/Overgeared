package net.stirdrem.overgeared.event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class QualityAttributeHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();

        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY);
        if (quality != null && quality != ForgingQuality.NONE)
            for (QualityAttributeDefinition def : QualityAttributeReloadListener.getAll()) {

                if (!matches(stack, def.targets()))
                    continue;

                QualityValue value = def.qualities().get(quality.getDisplayName());
                if (value == null || value.amount() == 0)
                    continue;
                Attribute attribute = BuiltInRegistries.ATTRIBUTE
                        .get(def.attribute());
                if (attribute == null)
                    continue;

                modifyAttribute(event, attribute, value.amount(), value.operation(), quality);
            }
    }

    public static boolean matches(ItemStack stack, List<QualityTarget> targets) {
        Item item = stack.getItem();

        for (QualityTarget target : targets) {
            switch (target.type()) {
                case WEAPON -> {
                    if (item instanceof TieredItem ||
                            item instanceof ProjectileWeaponItem) {
                        return true;
                    }
                }

                case ARMOR -> {
                    if (item instanceof ArmorItem) {
                        return true;
                    }
                }

                case ITEM -> {
                    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
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

    private static void modifyAttribute(ItemAttributeModifierEvent event, Attribute attribute, double bonus,
                                        AttributeModifier.Operation operation, ForgingQuality quality) {
        // Get all modifiers currently on the item
        var modifiers = event.getModifiers();

        // Filter for the target attribute
        var targetModifiers = new ArrayList<>(modifiers.stream()
                .filter(entry -> entry.attribute().is(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute)))
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
        ResourceLocation id;
        double amount;
        if (operation == AttributeModifier.Operation.ADD_VALUE) {
            id = original.id();
            amount = original.amount() + bonus;
        } else {
            id = ResourceLocation.parse(original.id() + "_overgeared_" + quality.getDisplayName() + "_"
                    + operation.name().toLowerCase(java.util.Locale.ROOT) + (int) bonus * 10);
            amount = bonus;
        }
        return new AttributeModifier(id, amount, operation);
    }
}