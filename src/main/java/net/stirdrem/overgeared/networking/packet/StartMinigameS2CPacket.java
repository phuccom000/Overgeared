package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;

/** S2C: start the anvil minigame at {@code pos} with {@code hits} hits targeting {@code quality}. */
public record StartMinigameS2CPacket(BlockPos pos, int hits, String quality) implements CustomPacketPayload {
    public static final Type<StartMinigameS2CPacket> TYPE = new Type<>(Overgeared.id("start_minigame"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StartMinigameS2CPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, StartMinigameS2CPacket::pos,
            ByteBufCodecs.INT, StartMinigameS2CPacket::hits,
            ByteBufCodecs.STRING_UTF8, StartMinigameS2CPacket::quality,
            StartMinigameS2CPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Client thread only. */
    public static void handle(StartMinigameS2CPacket msg) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        AnvilMinigameEvents.reset(msg.quality);
        AnvilMinigameEvents.setHitsRemaining(msg.hits);
        AnvilMinigameEvents.setAnvilPos(player.getUUID(), msg.pos);
        AnvilMinigameEvents.setMinigameStarted(msg.pos, true);
        AnvilMinigameEvents.setIsVisible(msg.pos, true);
    }
}
