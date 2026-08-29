package net.stirdrem.overgeared.datapack.quality_attribute;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public record QualityAttributeDefinition(
        Identifier attribute,
        List<QualityTarget> targets,
        Map<String, QualityValue> qualities
) {
}
