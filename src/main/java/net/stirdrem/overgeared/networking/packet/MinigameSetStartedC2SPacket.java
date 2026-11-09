package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.networking.ModMessages;

/** C2S: the client started the minigame at the anvil at {@code pos}. Answered with {@link MinigameSetStartedS2CPacket}. */
public record MinigameSetStartedC2SPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<MinigameSetStartedC2SPacket> TYPE = new Type<>(Overgeared.id("minigame_set_started"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MinigameSetStartedC2SPacket> STREAM_CODEC =
            BlockPos.STREAM_CODEC.<MinigameSetStartedC2SPacket>map(MinigameSetStartedC2SPacket::new, MinigameSetStartedC2SPacket::pos).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MinigameSetStartedC2SPacket msg, ServerPlayer sender) {
        BlockEntity be = sender.level().getBlockEntity(msg.pos);
        if (be instanceof AbstractSmithingAnvilBlockEntity anvilEntity) {
            // Upstream also calls client-only AnvilMinigameEvents.setMinigameStarted(...) here; that
            // would crash a dedicated server. The S2C ack drives the same client-side state update.
            ModMessages.sendToPlayer(sender, new MinigameSetStartedS2CPacket(msg.pos));
            ModItemInteractEvents.playerAnvilPositions.put(sender.getUUID(), msg.pos);
            ModItemInteractEvents.playerMinigameVisibility.put(sender.getUUID(), true);
            anvilEntity.setPlayer(sender);
            anvilEntity.setMinigameOn(true);
        }
    }
}
