package net.stirdrem.overgeared.datapack.quality_attribute;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record QualityValue(
        AttributeModifier.Operation operation,
        double amount
) {
}
