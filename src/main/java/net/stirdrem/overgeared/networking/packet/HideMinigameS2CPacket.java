package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;

/** S2C: hide the anvil minigame overlay for the receiving player. */
public record HideMinigameS2CPacket() implements CustomPacketPayload {
    public static final HideMinigameS2CPacket INSTANCE = new HideMinigameS2CPacket();
    public static final Type<HideMinigameS2CPacket> TYPE = new Type<>(Overgeared.id("hide_minigame"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HideMinigameS2CPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Client thread only. */
    public static void handle(HideMinigameS2CPacket msg) {
        try {
            var player = Minecraft.getInstance().player;
            if (player != null) AnvilMinigameEvents.hideMinigame(player.getUUID());
        } catch (Exception e) {
            Overgeared.LOGGER.error("Failed to process hide minigame packet", e);
        }
    }
}
