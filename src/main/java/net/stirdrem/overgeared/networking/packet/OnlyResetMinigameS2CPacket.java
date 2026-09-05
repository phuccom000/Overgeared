package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;

public class OnlyResetMinigameS2CPacket {

    public static void encode(OnlyResetMinigameS2CPacket msg, FriendlyByteBuf buf) {
    }

    public static OnlyResetMinigameS2CPacket decode(FriendlyByteBuf buf) {
        return new OnlyResetMinigameS2CPacket();
    }

    public static void handle(OnlyResetMinigameS2CPacket msg) {
        try {
            AnvilMinigameEvents.reset();
        } catch (Exception e) {
            Overgeared.LOGGER.error("Failed to process ResetMinigameS2CPacket for anvil", e);
        }
    }
}
