package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;

/** S2C: acknowledgement of {@link MinigameSetStartedC2SPacket}. */
public record MinigameSetStartedS2CPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<MinigameSetStartedS2CPacket> TYPE = new Type<>(Overgeared.id("minigame_set_started_ack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MinigameSetStartedS2CPacket> STREAM_CODEC =
            BlockPos.STREAM_CODEC.<MinigameSetStartedS2CPacket>map(MinigameSetStartedS2CPacket::new, MinigameSetStartedS2CPacket::pos).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Client thread only. */
    public static void handle(MinigameSetStartedS2CPacket msg) {
        AnvilMinigameEvents.setMinigameStarted(msg.pos, true);
        AnvilMinigameEvents.setIsVisible(msg.pos, true);
    }
}
