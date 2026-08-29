package net.stirdrem.overgeared.datapack.quality_attribute;

import net.minecraft.entity.attribute.EntityAttributeModifier;

public record QualityValue(
        EntityAttributeModifier.Operation operation,
        double amount
) {
}
