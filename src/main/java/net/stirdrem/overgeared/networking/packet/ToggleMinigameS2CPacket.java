package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

public class ToggleMinigameS2CPacket {

    private final BlockPos pos;
    private final boolean visible;

    public ToggleMinigameS2CPacket(BlockPos pos, boolean visible) {
        this.pos = pos;
        this.visible = visible;
    }

    public static void encode(ToggleMinigameS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.visible);
    }

    public static ToggleMinigameS2CPacket decode(FriendlyByteBuf buf) {
        return new ToggleMinigameS2CPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(ToggleMinigameS2CPacket msg) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        AnvilMinigameEvents.setIsVisible(msg.pos, msg.visible);
        ModItemInteractEvents.playerMinigameVisibility.put(player.getUUID(), msg.visible);
    }
}
