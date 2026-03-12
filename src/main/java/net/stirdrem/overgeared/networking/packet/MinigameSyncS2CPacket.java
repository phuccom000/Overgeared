package net.stirdrem.overgeared.networking.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

import java.util.function.Supplier;

public class MinigameSyncS2CPacket {
    private final CompoundTag minigameData;

    public MinigameSyncS2CPacket(CompoundTag minigameData) {
        this.minigameData = minigameData;
    }

    public MinigameSyncS2CPacket(FriendlyByteBuf buf) {
        this.minigameData = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(minigameData);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Validate the NBT data first
            if (minigameData == null) {
                OvergearedMod.LOGGER.error("Received null minigame data in packet");
                return;
            }

            try {
                // Update all client-side data at once
                ClientAnvilMinigameData.loadFromNBT(minigameData);
                ModItemInteractEvents.handleAnvilOwnershipSync(minigameData);
            } catch (Exception e) {
                OvergearedMod.LOGGER.error("Failed to process minigame sync packet", e);
            }
        });
        return true;
    }
}
