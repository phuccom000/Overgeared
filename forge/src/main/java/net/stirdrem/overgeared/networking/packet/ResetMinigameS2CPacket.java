package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;

import java.util.function.Supplier;

public record ResetMinigameS2CPacket(BlockPos anvilPos) {

    public ResetMinigameS2CPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(anvilPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            try {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    BlockEntity be = player.level().getBlockEntity(anvilPos);
                    if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
                        String quality = anvil.minigameQuality(); // ✅ get directly from BE
                        Overgeared.LOGGER.info(
                                "Resetting minigame for {} at anvil {} with quality {}",
                                player.getName().getString(), anvilPos, quality
                        );

                        // Only reset if the player's tracked anvil matches
                        if (ClientAnvilMinigameData.playerAnvilPositions
                                .getOrDefault(player.getUUID(), BlockPos.ZERO)
                                .equals(anvilPos)) {
                            ClientAnvilMinigameData.playerAnvilPositions.remove(player.getUUID());
                            ClientAnvilMinigameData.playerMinigameVisibility.remove(player.getUUID());
                            AnvilMinigameEvents.reset(quality);
                        }
                    }
                }
            } catch (Exception e) {
                Overgeared.LOGGER.error(
                        "Failed to process ResetMinigameS2CPacket for anvil at {}",
                        anvilPos, e
                );
            }
        });
        return true;
    }
}
