package net.stirdrem.overgeared.networking.packet;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

public class MinigameSyncS2CPacket {
    private final NbtCompound minigameData;

    public MinigameSyncS2CPacket(NbtCompound minigameData) {
        this.minigameData = minigameData;
    }

    public static void encode(MinigameSyncS2CPacket msg, PacketByteBuf buf) {
        buf.writeNbt(msg.minigameData);
    }

    public static MinigameSyncS2CPacket decode(PacketByteBuf buf) {
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
