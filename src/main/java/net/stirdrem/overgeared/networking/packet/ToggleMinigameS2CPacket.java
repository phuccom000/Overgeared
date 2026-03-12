package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

import java.util.function.Supplier;

import static net.stirdrem.overgeared.event.ModItemInteractEvents.playerMinigameVisibility;

public class ToggleMinigameS2CPacket {

    private final BlockPos pos;
    private final boolean visible;

    public ToggleMinigameS2CPacket(BlockPos pos, boolean visible) {
        this.pos = pos;
        this.visible = visible;
    }

    public ToggleMinigameS2CPacket(FriendlyByteBuf friendlyByteBuf) {
        this.pos = friendlyByteBuf.readBlockPos();
        this.visible = friendlyByteBuf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.visible);
    }

    public static void handle(ToggleMinigameS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            AnvilMinigameEvents.setIsVisible(msg.pos, msg.visible);
            playerMinigameVisibility.put(player.getUUID(), msg.visible);
        });

        ctx.get().setPacketHandled(true);
    }
}