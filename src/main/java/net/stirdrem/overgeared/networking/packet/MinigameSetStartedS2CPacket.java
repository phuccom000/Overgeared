package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;

public class MinigameSetStartedS2CPacket {
    private final BlockPos pos;

    public MinigameSetStartedS2CPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(MinigameSetStartedS2CPacket msg, PacketByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static MinigameSetStartedS2CPacket decode(PacketByteBuf buf) {
        return new MinigameSetStartedS2CPacket(buf.readBlockPos());
    }

    public static void handle(MinigameSetStartedS2CPacket msg) {
        AnvilMinigameEvents.setMinigameStarted(msg.pos, true);
        AnvilMinigameEvents.setIsVisible(msg.pos, true);
    }
}
