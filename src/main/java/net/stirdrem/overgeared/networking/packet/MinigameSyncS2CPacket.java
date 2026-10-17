package net.stirdrem.overgeared.networking.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

public class MinigameSyncS2CPacket {
    private final CompoundTag minigameData;

    public MinigameSyncS2CPacket(CompoundTag minigameData) {
        this.minigameData = minigameData;
    }

    public static void encode(MinigameSyncS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.minigameData);
    }

    public static MinigameSyncS2CPacket decode(FriendlyByteBuf buf) {
        return new MinigameSyncS2CPacket(buf.readNbt());
    }

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
