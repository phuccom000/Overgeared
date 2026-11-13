package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;
import net.stirdrem.overgeared.networking.PacketContext;
import net.stirdrem.overgeared.platform.Services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends the quality_attributes datapack definitions to a client. They are server data, so without this
 * only the host of a world (whose client shares the integrated server's copy) sees the quality bonuses
 * in item tooltips.
 */
public record SyncQualityAttributesS2CPacket(List<QualityAttributeDefinition> definitions) implements CustomPacketPayload {
    public static final Type<SyncQualityAttributesS2CPacket> TYPE = new Type<>(OvergearedMod.loc("sync_quality_attributes"));

    public static final StreamCodec<FriendlyByteBuf, SyncQualityAttributesS2CPacket> STREAM_CODEC = StreamCodec.of(
            SyncQualityAttributesS2CPacket::write, SyncQualityAttributesS2CPacket::read);

    private static void write(FriendlyByteBuf buf, SyncQualityAttributesS2CPacket packet) {
        buf.writeVarInt(packet.definitions.size());
        for (QualityAttributeDefinition def : packet.definitions) {
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

    private static SyncQualityAttributesS2CPacket read(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<QualityAttributeDefinition> definitions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Identifier attribute = readNullableId(buf);
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
        return new SyncQualityAttributesS2CPacket(definitions);
    }

    private static void writeNullableId(FriendlyByteBuf buf, Identifier id) {
        buf.writeBoolean(id != null);
        if (id != null) buf.writeIdentifier(id);
    }

    private static Identifier readNullableId(FriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readIdentifier() : null;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncQualityAttributesS2CPacket payload, PacketContext context) {
        context.enqueueWork(() -> QualityAttributeReloadListener.acceptSyncedDefinitions(payload.definitions()));
    }

    public static void sendTo(ServerPlayer player) {
        Services.PLATFORM.sendToPlayer(player, new SyncQualityAttributesS2CPacket(QualityAttributeReloadListener.getAll()));
    }
}
