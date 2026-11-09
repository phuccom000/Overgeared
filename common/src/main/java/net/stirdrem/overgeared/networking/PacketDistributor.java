package net.stirdrem.overgeared.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.platform.Services;

/**
 * Loader-independent packet sending.
 */
public final class PacketDistributor {
    private PacketDistributor() {
    }

    public static void sendToServer(CustomPacketPayload payload) {
        Services.PLATFORM.sendToServer(payload);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        Services.PLATFORM.sendToPlayer(player, payload);
    }

    public static void sendToAllPlayers(CustomPacketPayload payload) {
        Services.PLATFORM.sendToAllPlayers(payload);
    }
}
