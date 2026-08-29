package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;

public class HideMinigameS2CPacket {

    public static void encode(HideMinigameS2CPacket msg, PacketByteBuf buf) {
    }

    public static HideMinigameS2CPacket decode(PacketByteBuf buf) {
        return new HideMinigameS2CPacket();
    }

    public static void handle(HideMinigameS2CPacket msg) {
        try {
            var player = MinecraftClient.getInstance().player;
            if (player != null) AnvilMinigameEvents.hideMinigame(player.getUuid());
        } catch (Exception e) {
            Overgeared.LOGGER.error("Failed to process hide minigame packet", e);
        }
    }
}
