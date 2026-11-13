package net.stirdrem.overgeared.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.stirdrem.overgeared.networking.packet.*;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Every packet of the mod, for the loaders to register.
 */
public final class ModPackets {
    private ModPackets() {
    }

    public record Packet<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            BiConsumer<T, PacketContext> handler) {
    }

    public static final List<Packet<?>> CLIENTBOUND = List.of(
            new Packet<>(HideMinigameS2CPacket.TYPE, HideMinigameS2CPacket.STREAM_CODEC, HideMinigameS2CPacket::handle),
            new Packet<>(MinigameSetStartedS2CPacket.TYPE, MinigameSetStartedS2CPacket.STREAM_CODEC, MinigameSetStartedS2CPacket::handle),
            new Packet<>(MinigameSyncS2CPacket.TYPE, MinigameSyncS2CPacket.STREAM_CODEC, MinigameSyncS2CPacket::handle),
            new Packet<>(OnlyResetMinigameS2CPacket.TYPE, OnlyResetMinigameS2CPacket.STREAM_CODEC, OnlyResetMinigameS2CPacket::handle),
            new Packet<>(ResetMinigameS2CPacket.TYPE, ResetMinigameS2CPacket.STREAM_CODEC, ResetMinigameS2CPacket::handle),
            new Packet<>(StartMinigameS2CPacket.TYPE, StartMinigameS2CPacket.STREAM_CODEC, StartMinigameS2CPacket::handle),
            new Packet<>(ToggleMinigameS2CPacket.TYPE, ToggleMinigameS2CPacket.STREAM_CODEC, ToggleMinigameS2CPacket::handle),
            new Packet<>(SyncQualityAttributesS2CPacket.TYPE, SyncQualityAttributesS2CPacket.STREAM_CODEC, SyncQualityAttributesS2CPacket::handle)
    );

    public static final List<Packet<?>> SERVERBOUND = List.of(
            new Packet<>(KnappingChipC2SPacket.TYPE, KnappingChipC2SPacket.STREAM_CODEC, KnappingChipC2SPacket::handle),
            new Packet<>(MinigameSetStartedC2SPacket.TYPE, MinigameSetStartedC2SPacket.STREAM_CODEC, MinigameSetStartedC2SPacket::handle),
            new Packet<>(PacketSendCounterC2SPacket.TYPE, PacketSendCounterC2SPacket.STREAM_CODEC, PacketSendCounterC2SPacket::handle),
            new Packet<>(SelectToolTypeC2SPacket.TYPE, SelectToolTypeC2SPacket.STREAM_CODEC, SelectToolTypeC2SPacket::handle),
            new Packet<>(SetMinigameVisibleC2SPacket.TYPE, SetMinigameVisibleC2SPacket.STREAM_CODEC, SetMinigameVisibleC2SPacket::handle)
    );
}
