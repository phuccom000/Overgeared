package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;

public class StartMinigameS2CPacket {

    private final BlockPos pos;
    private final int hits;
    private final String quality;

    public StartMinigameS2CPacket(BlockPos pos, int hits, String quality) {
        this.pos = pos;
        this.hits = hits;
        this.quality = quality;
    }

    public static void encode(StartMinigameS2CPacket msg, PacketByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.hits);
        buf.writeString(msg.quality);
    }

    public static StartMinigameS2CPacket decode(PacketByteBuf buf) {
        return new StartMinigameS2CPacket(buf.readBlockPos(), buf.readInt(), buf.readString());
    }

    public static void handle(StartMinigameS2CPacket msg) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        AnvilMinigameEvents.reset(msg.quality);
        AnvilMinigameEvents.setHitsRemaining(msg.hits);
        AnvilMinigameEvents.setAnvilPos(player.getUuid(), msg.pos);
        AnvilMinigameEvents.setMinigameStarted(msg.pos, true);
        AnvilMinigameEvents.setIsVisible(msg.pos, true);
    }
}
