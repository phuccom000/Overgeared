package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

import java.util.function.Supplier;

public class StartMinigameS2CPacket {

    private final BlockPos pos;
    private final int hits;
    private final String quality;

    public StartMinigameS2CPacket(BlockPos pos, int hits, String quality) {
        this.pos = pos;
        this.hits = hits;
        this.quality = quality;
    }

    public StartMinigameS2CPacket(FriendlyByteBuf friendlyByteBuf) {
        this.pos = friendlyByteBuf.readBlockPos();
        this.hits = friendlyByteBuf.readInt();
        this.quality = friendlyByteBuf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeInt(this.hits);
        buf.writeUtf(this.quality);
    }

    public static void handle(StartMinigameS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            AnvilMinigameEvents.reset(msg.quality);
            AnvilMinigameEvents.setHitsRemaining(msg.hits);
            AnvilMinigameEvents.setAnvilPos(player.getUUID(), msg.pos);
            AnvilMinigameEvents.setMinigameStarted(msg.pos, true);
            AnvilMinigameEvents.setIsVisible(msg.pos, true);
        });

        ctx.get().setPacketHandled(true);
    }
}