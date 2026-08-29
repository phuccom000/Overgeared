package net.stirdrem.overgeared.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.*;

/**
 * Client-side half of the networking layer. ClientPlayNetworking is a client-only class, so
 * anything that sends C2S packets or registers S2C receivers has to live here rather than in
 * the common ModMessages - referencing it from code loaded on both sides risks a
 * NoClassDefFoundError on dedicated servers.
 */
public class ClientModMessages {

    public static void sendToServer(Identifier channel, PacketByteBuf buf) {
        ClientPlayNetworking.send(channel, buf);
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ModMessages.MINIGAME_SYNC, (client, handler, buf, responseSender) -> {
            MinigameSyncS2CPacket msg = MinigameSyncS2CPacket.decode(buf);
            client.execute(() -> MinigameSyncS2CPacket.handle(msg));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModMessages.MINIGAME_SET_STARTED_ACK, (client, handler, buf, responseSender) -> {
            MinigameSetStartedS2CPacket msg = MinigameSetStartedS2CPacket.decode(buf);
            client.execute(() -> MinigameSetStartedS2CPacket.handle(msg));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModMessages.START_MINIGAME, (client, handler, buf, responseSender) -> {
            StartMinigameS2CPacket msg = StartMinigameS2CPacket.decode(buf);
            client.execute(() -> StartMinigameS2CPacket.handle(msg));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModMessages.TOGGLE_MINIGAME, (client, handler, buf, responseSender) -> {
            ToggleMinigameS2CPacket msg = ToggleMinigameS2CPacket.decode(buf);
            client.execute(() -> ToggleMinigameS2CPacket.handle(msg));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModMessages.HIDE_MINIGAME, (client, handler, buf, responseSender) -> {
            HideMinigameS2CPacket msg = HideMinigameS2CPacket.decode(buf);
            client.execute(() -> HideMinigameS2CPacket.handle(msg));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModMessages.RESET_MINIGAME, (client, handler, buf, responseSender) -> {
            ResetMinigameS2CPacket msg = ResetMinigameS2CPacket.decode(buf);
            client.execute(() -> ResetMinigameS2CPacket.handle(msg));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModMessages.ONLY_RESET_MINIGAME, (client, handler, buf, responseSender) -> {
            OnlyResetMinigameS2CPacket msg = OnlyResetMinigameS2CPacket.decode(buf);
            client.execute(() -> OnlyResetMinigameS2CPacket.handle(msg));
        });
    }
}
