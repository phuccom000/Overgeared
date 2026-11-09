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
import net.stirdrem.overgeared.event.ModItemInteractEvents;

/** S2C: show/hide the minigame overlay of the anvil at {@code pos}. */
public record ToggleMinigameS2CPacket(BlockPos pos, boolean visible) implements CustomPacketPayload {
    public static final Type<ToggleMinigameS2CPacket> TYPE = new Type<>(Overgeared.id("toggle_minigame"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleMinigameS2CPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ToggleMinigameS2CPacket::pos,
            ByteBufCodecs.BOOL, ToggleMinigameS2CPacket::visible,
            ToggleMinigameS2CPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Client thread only. */
    public static void handle(ToggleMinigameS2CPacket msg) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        AnvilMinigameEvents.setIsVisible(msg.pos, msg.visible);
        ModItemInteractEvents.playerMinigameVisibility.put(player.getUUID(), msg.visible);
    }
}
