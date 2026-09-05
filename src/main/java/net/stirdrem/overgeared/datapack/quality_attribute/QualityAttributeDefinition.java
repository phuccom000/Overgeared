package net.stirdrem.overgeared.datapack.quality_attribute;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public record QualityAttributeDefinition(
        ResourceLocation attribute,
        List<QualityTarget> targets,
        Map<String, QualityValue> qualities
) {
}
