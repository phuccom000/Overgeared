package net.stirdrem.overgeared.datapack;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wire format for sending the quality_attributes datapack definitions to clients. They are server
 * data, so without this only the host of a world (whose client shares the integrated server's copy)
 * sees the quality bonuses in item tooltips.
 */
public final class QualityAttributeSync {
    private QualityAttributeSync() {
    }

    public static void write(FriendlyByteBuf buf) {
        List<QualityAttributeDefinition> definitions = QualityAttributeReloadListener.INSTANCE.getAll();
        buf.writeVarInt(definitions.size());
        for (QualityAttributeDefinition def : definitions) {
            writeNullableId(buf, def.attribute());
            buf.writeVarInt(def.targets().size());
            for (QualityTarget target : def.targets()) {
                buf.writeEnum(target.type());
                writeNullableId(buf, target.id());
            }
            buf.writeVarInt(def.qualities().size());
            for (Map.Entry<String, QualityValue> entry : def.qualities().entrySet()) {
                buf.writeUtf(entry.getKey());
                buf.writeEnum(entry.getValue().operation());
                buf.writeDouble(entry.getValue().amount());
            }
        }
    }

    public static List<QualityAttributeDefinition> read(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<QualityAttributeDefinition> definitions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation attribute = readNullableId(buf);
            int targetCount = buf.readVarInt();
            List<QualityTarget> targets = new ArrayList<>(targetCount);
            for (int j = 0; j < targetCount; j++) {
                targets.add(new QualityTarget(buf.readEnum(QualityTarget.TargetType.class), readNullableId(buf)));
            }
            int qualityCount = buf.readVarInt();
            Map<String, QualityValue> qualities = new HashMap<>();
            for (int j = 0; j < qualityCount; j++) {
                String quality = buf.readUtf();
                qualities.put(quality, new QualityValue(buf.readEnum(AttributeModifier.Operation.class), buf.readDouble()));
            }
            definitions.add(new QualityAttributeDefinition(attribute, targets, qualities));
        }
        return definitions;
    }

    private static void writeNullableId(FriendlyByteBuf buf, ResourceLocation id) {
        buf.writeBoolean(id != null);
        if (id != null) buf.writeResourceLocation(id);
    }

    private static ResourceLocation readNullableId(FriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readResourceLocation() : null;
    }
}
