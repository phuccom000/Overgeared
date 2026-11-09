package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

/** S2C: reset the minigame for the anvil at {@code anvilPos} if it is the player's tracked anvil. */
public record ResetMinigameS2CPacket(BlockPos anvilPos) implements CustomPacketPayload {
    public static final Type<ResetMinigameS2CPacket> TYPE = new Type<>(Overgeared.id("reset_minigame"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResetMinigameS2CPacket> STREAM_CODEC =
            BlockPos.STREAM_CODEC.<ResetMinigameS2CPacket>map(ResetMinigameS2CPacket::new, ResetMinigameS2CPacket::anvilPos).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Client thread only. */
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
            Overgeared.LOGGER.error("Failed to process ResetMinigameS2CPacket for anvil at {}", msg.anvilPos, e);
        }
    }

    public BlockPos getAnvilPos() {
        return anvilPos;
    }
}
