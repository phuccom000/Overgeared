package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.stirdrem.overgeared.networking.PacketContext;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

public record MinigameSetStartedS2CPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Identifier ID = OvergearedMod.loc("minigame_set_started_s2c");
    public static final CustomPacketPayload.Type<MinigameSetStartedS2CPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, MinigameSetStartedS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> buffer.writeBlockPos(packet.pos),
            buffer -> new MinigameSetStartedS2CPacket(buffer.readBlockPos())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MinigameSetStartedS2CPacket payload, PacketContext context) {
        context.enqueueWork(() -> {
            AnvilMinigameEvents.setMinigameStarted(payload.pos, true);
            AnvilMinigameEvents.setIsVisible(payload.pos, true);
        });
    }
}
