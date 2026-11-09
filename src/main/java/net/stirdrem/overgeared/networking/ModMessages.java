package net.stirdrem.overgeared.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.networking.packet.*;

/**
 * Central place for all Overgeared network payloads (see README.md in this package).
 *
 * <p>Every packet is a {@code record} implementing {@link CustomPacketPayload} with a {@code TYPE} and
 * {@code STREAM_CODEC}. {@link #register()} (common init) registers every payload type for both
 * directions plus the server-side receivers. {@link #registerClient()} must be called from the client
 * entrypoint to register the client-side receivers.
 *
 * <p>Client-only code (ClientPlayNetworking) lives in {@link ModMessagesClient}; the {@code sendToServer}
 * helpers here only touch that class when called, so this class stays safe to load on a dedicated server.
 */
public class ModMessages {

    public static void register() {
        // ---- client -> server ----
        PayloadTypeRegistry.serverboundPlay().register(KnappingChipC2SPacket.TYPE, KnappingChipC2SPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SelectToolTypeC2SPacket.TYPE, SelectToolTypeC2SPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PacketSendCounterC2SPacket.TYPE, PacketSendCounterC2SPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetMinigameVisibleC2SPacket.TYPE, SetMinigameVisibleC2SPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MinigameSetStartedC2SPacket.TYPE, MinigameSetStartedC2SPacket.STREAM_CODEC);

        // ---- server -> client ----
        PayloadTypeRegistry.clientboundPlay().register(MinigameSyncS2CPacket.TYPE, MinigameSyncS2CPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MinigameSetStartedS2CPacket.TYPE, MinigameSetStartedS2CPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StartMinigameS2CPacket.TYPE, StartMinigameS2CPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ToggleMinigameS2CPacket.TYPE, ToggleMinigameS2CPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(HideMinigameS2CPacket.TYPE, HideMinigameS2CPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ResetMinigameS2CPacket.TYPE, ResetMinigameS2CPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OnlyResetMinigameS2CPacket.TYPE, OnlyResetMinigameS2CPacket.STREAM_CODEC);

        // Server receivers run on the server thread.
        ServerPlayNetworking.registerGlobalReceiver(KnappingChipC2SPacket.TYPE,
                (payload, context) -> KnappingChipC2SPacket.handle(payload, context.player()));
        ServerPlayNetworking.registerGlobalReceiver(SelectToolTypeC2SPacket.TYPE,
                (payload, context) -> SelectToolTypeC2SPacket.handle(payload, context.player()));
        ServerPlayNetworking.registerGlobalReceiver(PacketSendCounterC2SPacket.TYPE,
                (payload, context) -> PacketSendCounterC2SPacket.handle(payload, context.player()));
        ServerPlayNetworking.registerGlobalReceiver(SetMinigameVisibleC2SPacket.TYPE,
                (payload, context) -> SetMinigameVisibleC2SPacket.handle(payload, context.player()));
        ServerPlayNetworking.registerGlobalReceiver(MinigameSetStartedC2SPacket.TYPE,
                (payload, context) -> MinigameSetStartedC2SPacket.handle(payload, context.player()));
    }

    /** Registers the client-side (S2C) receivers. Call from the client entrypoint only. */
    public static void registerClient() {
        ModMessagesClient.register();
    }

    // ------------------------------------------------------------------
    // Generic send helpers
    // ------------------------------------------------------------------

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendToAll(MinecraftServer server, CustomPacketPayload payload) {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    /** Client side only. */
    public static void sendToServer(CustomPacketPayload payload) {
        ModMessagesClient.sendToServer(payload);
    }

    // ------------------------------------------------------------------
    // Typed helpers (server -> client)
    // ------------------------------------------------------------------

    public static void sendMinigameSync(MinecraftServer server, CompoundTag data) {
        sendToAll(server, new MinigameSyncS2CPacket(data));
    }

    public static void sendStartMinigame(ServerPlayer player, BlockPos pos, int hits, String quality) {
        sendToPlayer(player, new StartMinigameS2CPacket(pos, hits, quality));
    }

    public static void sendToggleMinigame(ServerPlayer player, BlockPos pos, boolean visible) {
        sendToPlayer(player, new ToggleMinigameS2CPacket(pos, visible));
    }

    public static void sendHideMinigame(ServerPlayer player) {
        sendToPlayer(player, HideMinigameS2CPacket.INSTANCE);
    }

    public static void sendResetMinigame(ServerPlayer player, BlockPos anvilPos) {
        sendToPlayer(player, new ResetMinigameS2CPacket(anvilPos));
    }

    public static void sendOnlyResetMinigame(ServerPlayer player) {
        sendToPlayer(player, OnlyResetMinigameS2CPacket.INSTANCE);
    }

    // ------------------------------------------------------------------
    // Typed helpers (client -> server); call from client code only
    // ------------------------------------------------------------------

    public static void sendKnappingChip(int index) {
        sendToServer(new KnappingChipC2SPacket(index));
    }

    public static void sendSelectToolType(String toolTypeId, int containerId) {
        sendToServer(new SelectToolTypeC2SPacket(toolTypeId, containerId));
    }

    public static void sendCounter(BlockPos pos, String quality) {
        sendToServer(new PacketSendCounterC2SPacket(pos, quality));
    }

    public static void sendSetMinigameVisible(BlockPos pos, boolean visible) {
        sendToServer(new SetMinigameVisibleC2SPacket(pos, visible));
    }

    public static void sendMinigameSetStarted(BlockPos pos) {
        sendToServer(new MinigameSetStartedC2SPacket(pos));
    }
}
