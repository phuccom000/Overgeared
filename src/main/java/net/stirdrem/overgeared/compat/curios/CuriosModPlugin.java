package net.stirdrem.overgeared.compat.curios;

import com.google.common.collect.Multimap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

import java.util.List;

public class CuriosModPlugin {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCurioAttributes(CurioAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();

        if (!stack.hasTag() || !stack.getTag().contains("ForgingQuality")) return;

        String quality = stack.getTag().getString("ForgingQuality");

        for (QualityAttributeDefinition def :
                QualityAttributeReloadListener.INSTANCE.getAll()) {

            if (!matches(stack, def.targets())) continue;

            QualityValue value = def.qualities().get(quality);
            if (value == null || value.amount() == 0) continue;
            Attribute attribute = ForgeRegistries.ATTRIBUTES
                    .getValue(def.attribute());
            if (attribute == null) continue;

            addCurioModifier(event, attribute, value.amount(), value.operation());
        }

    }


    private static void addCurioModifier(CurioAttributeModifierEvent event, Attribute attribute, double bonus, AttributeModifier.Operation operation) {
        Multimap<Attribute, AttributeModifier> originalModifiers = event.getModifiers();

        if (!originalModifiers.containsKey(attribute)) return;

        List<AttributeModifier> modifiers = List.copyOf(originalModifiers.get(attribute));

        for (AttributeModifier modifier : modifiers) {
            if (modifier.getAmount() == 0) continue;

            event.removeModifier(attribute, modifier);
            event.addModifier(attribute, createModifiedAttribute(modifier, bonus, operation));
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

    private static AttributeModifier createModifiedAttribute(AttributeModifier original, double bonus, AttributeModifier.Operation operation) {
        return new AttributeModifier(
                original.getId(),
                "Overgeared",
                original.getAmount() + bonus,
                operation
        );
    }
}
