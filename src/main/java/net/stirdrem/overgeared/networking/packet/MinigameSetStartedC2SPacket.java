package net.stirdrem.overgeared.networking.packet;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.networking.ModMessages;

public class MinigameSetStartedC2SPacket {
    private final BlockPos pos;

    public MinigameSetStartedC2SPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(MinigameSetStartedC2SPacket pkt, PacketByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static MinigameSetStartedC2SPacket decode(PacketByteBuf buf) {
        return new MinigameSetStartedC2SPacket(buf.readBlockPos());
    }


    public static void handle(MinigameSetStartedC2SPacket msg, MinecraftServer server, ServerPlayerEntity sender) {
        server.execute(() -> {
            BlockEntity be = sender.getWorld().getBlockEntity(msg.pos);
            if (be instanceof AbstractSmithingAnvilBlockEntity anvilEntity) {
                // Upstream also calls AnvilMinigameEvents.setMinigameStarted(...) directly here,
                // but that's a client-only class (imports net.minecraft.client.Minecraft) being
                // touched from a C2S handler, which only ever runs server-side - that would throw
                // NoClassDefFoundError on a real dedicated server (works in singleplayer only,
                // since the integrated server shares a JVM with the client). The S2C ack below
                // already drives the same client-side state update safely.
                PacketByteBuf out = ModMessages.buf();
                MinigameSetStartedS2CPacket.encode(new MinigameSetStartedS2CPacket(msg.pos), out);
                ModMessages.sendToPlayer(ModMessages.MINIGAME_SET_STARTED_ACK, out, sender);
                ModItemInteractEvents.playerAnvilPositions.put(sender.getUuid(), msg.pos);
                ModItemInteractEvents.playerMinigameVisibility.put(sender.getUuid(), true);
                anvilEntity.setPlayer(sender);
                anvilEntity.setMinigameOn(true);
            }
        });
    }


}
