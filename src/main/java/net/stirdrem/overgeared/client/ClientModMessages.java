package net.stirdrem.overgeared.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.stirdrem.overgeared.networking.ModMessages;

/**
 * Client-side half of the networking layer. ClientPlayNetworking is a client-only class, so
 * anything that sends C2S packets has to live in client code rather than in the common
 * ModMessages - referencing it from code loaded on both sides risks a NoClassDefFoundError on
 * dedicated servers.
 *
 * <p>26.3: packets are {@link CustomPacketPayload} records; the S2C receivers are registered by
 * the networking package ({@code ModMessages.registerClient()}).
 */
public class ClientModMessages {

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    public static void register() {
        ModMessages.registerClient();
    }
}
