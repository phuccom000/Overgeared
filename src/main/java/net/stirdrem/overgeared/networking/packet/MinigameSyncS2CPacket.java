package net.stirdrem.overgeared.networking.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

/** S2C: anvil ownership / minigame state sync (free-form NBT: "anvilOwner" UUID, "anvilPos" long). */
public record MinigameSyncS2CPacket(CompoundTag minigameData) implements CustomPacketPayload {
    public static final Type<MinigameSyncS2CPacket> TYPE = new Type<>(Overgeared.id("minigame_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MinigameSyncS2CPacket> STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.<MinigameSyncS2CPacket>map(MinigameSyncS2CPacket::new, MinigameSyncS2CPacket::minigameData).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Client thread only. */
    public static void handle(MinigameSyncS2CPacket msg) {
        if (msg.minigameData == null) {
            Overgeared.LOGGER.error("Received null minigame data in packet");
            return;
        }

        try {
            ClientAnvilMinigameData.loadFromNbt(msg.minigameData);
            ModItemInteractEvents.handleAnvilOwnershipSync(msg.minigameData);
        } catch (Exception e) {
            Overgeared.LOGGER.error("Failed to process minigame sync packet", e);
        }
    }
}
