package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

public class ResetMinigameS2CPacket {
    private final BlockPos anvilPos;

    public ResetMinigameS2CPacket(BlockPos anvilPos) {
        this.anvilPos = anvilPos;
    }

    public static void encode(ResetMinigameS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.anvilPos);
    }

    public static ResetMinigameS2CPacket decode(FriendlyByteBuf buf) {
        return new ResetMinigameS2CPacket(buf.readBlockPos());
    }

    public static void handle(ResetMinigameS2CPacket msg) {
        try {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                BlockEntity be = player.level().getBlockEntity(msg.anvilPos);
                if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
                    String quality = anvil.minigameQuality();
                    Overgeared.LOGGER.info(
                            "Resetting minigame for {} at anvil {} with quality {}",
                            player.getName().getString(), msg.anvilPos, quality
                    );

                    // Only reset if the player's tracked anvil matches
                    if (ModItemInteractEvents.playerAnvilPositions
                            .getOrDefault(player.getUUID(), BlockPos.ZERO)
                            .equals(msg.anvilPos)) {
                        ModItemInteractEvents.playerAnvilPositions.remove(player.getUUID());
                        ModItemInteractEvents.playerMinigameVisibility.remove(player.getUUID());
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
