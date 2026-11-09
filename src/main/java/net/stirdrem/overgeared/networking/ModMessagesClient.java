package net.stirdrem.overgeared.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.stirdrem.overgeared.networking.packet.*;

/**
 * Client half of {@link ModMessages}. Only loaded on the client (ClientPlayNetworking is client-only).
 * Fabric invokes play receivers on the render thread, so handlers may touch client state directly.
 */
@Environment(EnvType.CLIENT)
public final class ModMessagesClient {
    private ModMessagesClient() {
    }

    static void register() {
        ClientPlayNetworking.registerGlobalReceiver(MinigameSyncS2CPacket.TYPE, (payload, context) -> MinigameSyncS2CPacket.handle(payload));
        ClientPlayNetworking.registerGlobalReceiver(MinigameSetStartedS2CPacket.TYPE, (payload, context) -> MinigameSetStartedS2CPacket.handle(payload));
        ClientPlayNetworking.registerGlobalReceiver(StartMinigameS2CPacket.TYPE, (payload, context) -> StartMinigameS2CPacket.handle(payload));
        ClientPlayNetworking.registerGlobalReceiver(ToggleMinigameS2CPacket.TYPE, (payload, context) -> ToggleMinigameS2CPacket.handle(payload));
        ClientPlayNetworking.registerGlobalReceiver(HideMinigameS2CPacket.TYPE, (payload, context) -> HideMinigameS2CPacket.handle(payload));
        ClientPlayNetworking.registerGlobalReceiver(ResetMinigameS2CPacket.TYPE, (payload, context) -> ResetMinigameS2CPacket.handle(payload));
        ClientPlayNetworking.registerGlobalReceiver(OnlyResetMinigameS2CPacket.TYPE, (payload, context) -> OnlyResetMinigameS2CPacket.handle(payload));
    }

    static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
