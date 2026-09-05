package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.networking.ModMessages;

public class MinigameSetStartedC2SPacket {
    private final BlockPos pos;

    public MinigameSetStartedC2SPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(MinigameSetStartedC2SPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static MinigameSetStartedC2SPacket decode(FriendlyByteBuf buf) {
        return new MinigameSetStartedC2SPacket(buf.readBlockPos());
    }


    public static void handle(MinigameSetStartedC2SPacket msg, MinecraftServer server, ServerPlayer sender) {
        server.execute(() -> {
            BlockEntity be = sender.level().getBlockEntity(msg.pos);
            if (be instanceof AbstractSmithingAnvilBlockEntity anvilEntity) {
                // Upstream also calls AnvilMinigameEvents.setMinigameStarted(...) directly here,
                // but that's a client-only class (imports net.minecraft.client.Minecraft) being
                // touched from a C2S handler, which only ever runs server-side - that would throw
                // NoClassDefFoundError on a real dedicated server (works in singleplayer only,
                // since the integrated server shares a JVM with the client). The S2C ack below
                // already drives the same client-side state update safely.
                FriendlyByteBuf out = ModMessages.buf();
                MinigameSetStartedS2CPacket.encode(new MinigameSetStartedS2CPacket(msg.pos), out);
                ModMessages.sendToPlayer(ModMessages.MINIGAME_SET_STARTED_ACK, out, sender);
                ModItemInteractEvents.playerAnvilPositions.put(sender.getUUID(), msg.pos);
                ModItemInteractEvents.playerMinigameVisibility.put(sender.getUUID(), true);
                anvilEntity.setPlayer(sender);
                anvilEntity.setMinigameOn(true);
            }
        });
    }


}
