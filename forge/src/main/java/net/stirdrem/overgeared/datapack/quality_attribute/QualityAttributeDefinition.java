package net.stirdrem.overgeared.datapack.quality_attribute;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public record QualityAttributeDefinition(
        ResourceLocation attribute,
        List<QualityTarget> targets,
        Map<String, QualityValue> qualities
) {
}
