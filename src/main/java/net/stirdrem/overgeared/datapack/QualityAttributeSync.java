package net.stirdrem.overgeared.datapack;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.entity.attribute.EntityAttributeModifier;
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

    public static void write(PacketByteBuf buf) {
        List<QualityAttributeDefinition> definitions = QualityAttributeReloadListener.INSTANCE.getAll();
        buf.writeVarInt(definitions.size());
        for (QualityAttributeDefinition def : definitions) {
            writeNullableId(buf, def.attribute());
            buf.writeVarInt(def.targets().size());
            for (QualityTarget target : def.targets()) {
                buf.writeEnumConstant(target.type());
                writeNullableId(buf, target.id());
            }
            buf.writeVarInt(def.qualities().size());
            for (Map.Entry<String, QualityValue> entry : def.qualities().entrySet()) {
                buf.writeString(entry.getKey());
                buf.writeEnumConstant(entry.getValue().operation());
                buf.writeDouble(entry.getValue().amount());
            }
        }
    }

    public static List<QualityAttributeDefinition> read(PacketByteBuf buf) {
        int count = buf.readVarInt();
        List<QualityAttributeDefinition> definitions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Identifier attribute = readNullableId(buf);
            int targetCount = buf.readVarInt();
            List<QualityTarget> targets = new ArrayList<>(targetCount);
            for (int j = 0; j < targetCount; j++) {
                targets.add(new QualityTarget(buf.readEnumConstant(QualityTarget.TargetType.class), readNullableId(buf)));
            }
            int qualityCount = buf.readVarInt();
            Map<String, QualityValue> qualities = new HashMap<>();
            for (int j = 0; j < qualityCount; j++) {
                String quality = buf.readString();
                qualities.put(quality, new QualityValue(buf.readEnumConstant(EntityAttributeModifier.Operation.class), buf.readDouble()));
            }
            definitions.add(new QualityAttributeDefinition(attribute, targets, qualities));
        }
        return definitions;
    }

    private static void writeNullableId(PacketByteBuf buf, Identifier id) {
        buf.writeBoolean(id != null);
        if (id != null) buf.writeIdentifier(id);
    }

    private static Identifier readNullableId(PacketByteBuf buf) {
        return buf.readBoolean() ? buf.readIdentifier() : null;
    }
}
