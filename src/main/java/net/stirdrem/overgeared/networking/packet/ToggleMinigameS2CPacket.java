package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

public class ToggleMinigameS2CPacket {

    private final BlockPos pos;
    private final boolean visible;

    public ToggleMinigameS2CPacket(BlockPos pos, boolean visible) {
        this.pos = pos;
        this.visible = visible;
    }

    public static void encode(ToggleMinigameS2CPacket msg, PacketByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.visible);
    }

    public static ToggleMinigameS2CPacket decode(PacketByteBuf buf) {
        return new ToggleMinigameS2CPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(ToggleMinigameS2CPacket msg) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        AnvilMinigameEvents.setIsVisible(msg.pos, msg.visible);
        ModItemInteractEvents.playerMinigameVisibility.put(player.getUuid(), msg.visible);
    }
}
