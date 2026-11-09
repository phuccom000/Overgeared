package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.networking.PacketContext;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

import java.util.UUID;

/**
 * Tells a client to reset its minigame for the given anvil.
 * The quality is computed on the server, since the anvil's recipe/inventory may not be in sync on the client.
 */
public record ResetMinigameS2CPacket(BlockPos anvilPos, String quality) implements CustomPacketPayload {
    public static final ResourceLocation ID = OvergearedMod.loc("reset_minigame");
    public static final CustomPacketPayload.Type<ResetMinigameS2CPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ResetMinigameS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                BlockPos.STREAM_CODEC.encode(buffer, packet.anvilPos);
                buffer.writeUtf(packet.quality);
            },
            buffer -> new ResetMinigameS2CPacket(BlockPos.STREAM_CODEC.decode(buffer), buffer.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResetMinigameS2CPacket payload, PacketContext context) {
        context.enqueueWork(() -> {
            UUID playerId = context.player().getUUID();
            BlockPos tracked = ModItemInteractEvents.playerAnvilPositions.get(playerId);

            // Skip only if this client is busy at a *different* anvil. No tracked anvil is fine:
            // in single-player the server shares this map and may already have removed the entry.
            if (tracked != null && !tracked.equals(payload.anvilPos)) return;

            OvergearedMod.LOGGER.debug("Resetting minigame at anvil {} with quality {}", payload.anvilPos, payload.quality);
            ModItemInteractEvents.playerAnvilPositions.remove(playerId);
            ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
            AnvilMinigameEvents.reset(payload.quality);
        });
    }
}
