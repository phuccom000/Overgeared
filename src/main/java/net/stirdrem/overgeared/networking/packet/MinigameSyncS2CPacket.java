package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

import java.util.UUID;
import java.util.function.Supplier;

public class MinigameSyncS2CPacket {
    private final CompoundTag minigameData;
    private final BlockPos anvilPos;
    private final UUID ownerUUID;
    private final boolean clearOwnership;

    // Constructor for full minigame state sync
    public MinigameSyncS2CPacket(CompoundTag minigameData) {
        this.minigameData = minigameData;
        this.anvilPos = null;
        this.ownerUUID = null;
        this.clearOwnership = false;
    }

    // Constructor for ownership-only sync (used when releasing anvil)
    public MinigameSyncS2CPacket(BlockPos anvilPos, UUID ownerUUID) {
        this.minigameData = null;
        this.anvilPos = anvilPos;
        this.ownerUUID = ownerUUID;
        this.clearOwnership = (ownerUUID == null ||
                (ownerUUID.getMostSignificantBits() == 0 && ownerUUID.getLeastSignificantBits() == 0));
    }

    public MinigameSyncS2CPacket(FriendlyByteBuf buf) {
        // Read whether this is a full sync or ownership-only sync
        boolean isFullSync = buf.readBoolean();

        if (isFullSync) {
            this.minigameData = buf.readNbt();
            this.anvilPos = null;
            this.ownerUUID = null;
            this.clearOwnership = false;
        } else {
            this.minigameData = null;
            this.anvilPos = buf.readBlockPos();

            // Read owner UUID (could be null)
            if (buf.readBoolean()) {
                this.ownerUUID = buf.readUUID();
            } else {
                this.ownerUUID = null;
            }
            this.clearOwnership = buf.readBoolean();
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        // Write whether this is a full sync
        boolean isFullSync = (minigameData != null);
        buf.writeBoolean(isFullSync);

        if (isFullSync) {
            buf.writeNbt(minigameData);
        } else {
            buf.writeBlockPos(anvilPos);

            // Write owner UUID (with null flag)
            buf.writeBoolean(ownerUUID != null);
            if (ownerUUID != null) {
                buf.writeUUID(ownerUUID);
            }
            buf.writeBoolean(clearOwnership);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            try {
                if (minigameData != null) {
                    // Full minigame sync
                    handleFullSync();
                } else {
                    // Ownership-only sync
                    handleOwnershipSync();
                }
            } catch (Exception e) {
                OvergearedMod.LOGGER.error("Failed to process minigame sync packet", e);
            }
        });
        return true;
    }

    private void handleFullSync() {
        // Validate the NBT data first
        if (minigameData == null || minigameData.isEmpty()) {
            OvergearedMod.LOGGER.debug("Received empty minigame data in packet");
            return;
        }

        // Update all client-side minigame data
        ClientAnvilMinigameData.loadFromNBT(minigameData);

        // Handle ownership sync if present in the data
        if (minigameData.contains("anvilPos") && minigameData.contains("anvilOwner")) {
            BlockPos pos = BlockPos.of(minigameData.getLong("anvilPos"));
            UUID owner = minigameData.getUUID("anvilOwner");

            // Check for null UUID (all zeros)
            if (owner.getMostSignificantBits() == 0 && owner.getLeastSignificantBits() == 0) {
                ClientAnvilMinigameData.putOccupiedAnvil(pos, null);
            } else {
                ClientAnvilMinigameData.putOccupiedAnvil(pos, owner);
            }

            // Trigger the existing handler for backward compatibility
            ModItemInteractEvents.handleAnvilOwnershipSync(minigameData);
        }

        OvergearedMod.LOGGER.debug("Successfully synced full minigame state to client");
    }

    private void handleOwnershipSync() {
        if (anvilPos == null) {
            OvergearedMod.LOGGER.error("Received ownership sync packet with null position");
            return;
        }

        if (clearOwnership) {
            // Clear ownership
            ClientAnvilMinigameData.putOccupiedAnvil(anvilPos, null);

            // Also clear any pending minigame at this position
            if (anvilPos.equals(ClientAnvilMinigameData.getPendingMinigamePos())) {
                ClientAnvilMinigameData.clearPendingMinigame();
            }

            OvergearedMod.LOGGER.debug("Cleared ownership for anvil at {}", anvilPos);
        } else {
            // Set new owner
            ClientAnvilMinigameData.putOccupiedAnvil(anvilPos, ownerUUID);

            // If this client is the new owner and was waiting for this anvil, trigger minigame start
            if (ownerUUID != null &&
                    Minecraft.getInstance().player != null &&
                    ownerUUID.equals(Minecraft.getInstance().player.getUUID()) &&
                    anvilPos.equals(ClientAnvilMinigameData.getPendingMinigamePos())) {

                ClientAnvilMinigameData.clearPendingMinigame();
                OvergearedMod.LOGGER.debug("Client is now owner of anvil at {}, ready to start minigame", anvilPos);
            }

            OvergearedMod.LOGGER.debug("Set ownership for anvil at {} to {}", anvilPos, ownerUUID);
        }

        // Trigger the existing handler for backward compatibility
        CompoundTag legacyData = new CompoundTag();
        legacyData.putLong("anvilPos", anvilPos.asLong());
        if (clearOwnership) {
            legacyData.putUUID("anvilOwner", new UUID(0, 0));
        } else if (ownerUUID != null) {
            legacyData.putUUID("anvilOwner", ownerUUID);
        }
        ModItemInteractEvents.handleAnvilOwnershipSync(legacyData);
    }
}