package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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

    public static void encode(StartMinigameS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.hits);
        buf.writeUtf(msg.quality);
    }

    public static StartMinigameS2CPacket decode(FriendlyByteBuf buf) {
        return new StartMinigameS2CPacket(buf.readBlockPos(), buf.readInt(), buf.readUtf());
    }

    public static void handle(StartMinigameS2CPacket msg) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        AnvilMinigameEvents.reset(msg.quality);
        AnvilMinigameEvents.setHitsRemaining(msg.hits);
        AnvilMinigameEvents.setAnvilPos(player.getUUID(), msg.pos);
        AnvilMinigameEvents.setMinigameStarted(msg.pos, true);
        AnvilMinigameEvents.setIsVisible(msg.pos, true);
    }
}
