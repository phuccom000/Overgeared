package net.stirdrem.overgeared.networking.packet;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

public class ResetMinigameS2CPacket {
    private final BlockPos anvilPos;

    public ResetMinigameS2CPacket(BlockPos anvilPos) {
        this.anvilPos = anvilPos;
    }

    public static void encode(ResetMinigameS2CPacket msg, PacketByteBuf buf) {
        buf.writeBlockPos(msg.anvilPos);
    }

    public static ResetMinigameS2CPacket decode(PacketByteBuf buf) {
        return new ResetMinigameS2CPacket(buf.readBlockPos());
    }

    public static void handle(ResetMinigameS2CPacket msg) {
        try {
            var player = MinecraftClient.getInstance().player;
            if (player != null) {
                BlockEntity be = player.getWorld().getBlockEntity(msg.anvilPos);
                if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
                    String quality = anvil.minigameQuality();
                    Overgeared.LOGGER.info(
                            "Resetting minigame for {} at anvil {} with quality {}",
                            player.getName().getString(), msg.anvilPos, quality
                    );

                    // Only reset if the player's tracked anvil matches
                    if (ModItemInteractEvents.playerAnvilPositions
                            .getOrDefault(player.getUuid(), BlockPos.ORIGIN)
                            .equals(msg.anvilPos)) {
                        ModItemInteractEvents.playerAnvilPositions.remove(player.getUuid());
                        ModItemInteractEvents.playerMinigameVisibility.remove(player.getUuid());
                        AnvilMinigameEvents.reset(quality);
                    }
                }
            }
        } catch (Exception e) {
            Overgeared.LOGGER.error(
                    "Failed to process ResetMinigameS2CPacket for anvil at {}",
                    msg.anvilPos, e
            );
        }
    }

    public BlockPos getAnvilPos() {
        return anvilPos;
    }
}
