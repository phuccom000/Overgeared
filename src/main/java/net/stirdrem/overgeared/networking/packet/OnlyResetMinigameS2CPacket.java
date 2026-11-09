package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;

/** S2C: reset the client-side minigame state without touching anvil ownership. */
public record OnlyResetMinigameS2CPacket() implements CustomPacketPayload {
    public static final OnlyResetMinigameS2CPacket INSTANCE = new OnlyResetMinigameS2CPacket();
    public static final Type<OnlyResetMinigameS2CPacket> TYPE = new Type<>(Overgeared.id("only_reset_minigame"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OnlyResetMinigameS2CPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Client thread only. */
    public static void handle(OnlyResetMinigameS2CPacket msg) {
        try {
            AnvilMinigameEvents.reset();
        } catch (Exception e) {
            Overgeared.LOGGER.error("Failed to process OnlyResetMinigameS2CPacket", e);
        }
    }
}
