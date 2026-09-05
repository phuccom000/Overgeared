package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;

public class HideMinigameS2CPacket {

    public static void encode(HideMinigameS2CPacket msg, FriendlyByteBuf buf) {
    }

    public static HideMinigameS2CPacket decode(FriendlyByteBuf buf) {
        return new HideMinigameS2CPacket();
    }

    public static void handle(HideMinigameS2CPacket msg) {
        try {
            var player = Minecraft.getInstance().player;
            if (player != null) AnvilMinigameEvents.hideMinigame(player.getUUID());
        } catch (Exception e) {
            Overgeared.LOGGER.error("Failed to process hide minigame packet", e);
        }
    }
}
