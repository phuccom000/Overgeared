package net.stirdrem.overgeared.datapack.quality_attribute;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;

public record QualityAttributeDefinition(
        Identifier attribute,
        List<QualityTarget> targets,
        Map<String, QualityValue> qualities
) {
}
