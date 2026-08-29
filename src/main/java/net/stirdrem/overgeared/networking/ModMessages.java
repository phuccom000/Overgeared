package net.stirdrem.overgeared.networking;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.networking.packet.*;

/**
 * Fabric has no equivalent of Forge's SimpleChannel (auto-incrementing numeric packet IDs on
 * one shared channel); each payload gets its own named Identifier channel instead, sent via
 * ServerPlayNetworking/ClientPlayNetworking. Server-bound (C2S) receivers are registered here;
 * client-bound (S2C) receivers are registered from OvergearedClient (client-only code can't
 * live in common).
 */
public class ModMessages {
    public static final Identifier MINIGAME_SYNC = Overgeared.id("minigame_sync");
    public static final Identifier KNAPPING_CHIP = Overgeared.id("knapping_chip");
    public static final Identifier SELECT_TOOL_TYPE = Overgeared.id("select_tool_type");
    public static final Identifier SEND_COUNTER = Overgeared.id("send_counter");
    public static final Identifier SET_MINIGAME_VISIBLE = Overgeared.id("set_minigame_visible");
    public static final Identifier MINIGAME_SET_STARTED = Overgeared.id("minigame_set_started");
    public static final Identifier MINIGAME_SET_STARTED_ACK = Overgeared.id("minigame_set_started_ack");
    public static final Identifier START_MINIGAME = Overgeared.id("start_minigame");
    public static final Identifier TOGGLE_MINIGAME = Overgeared.id("toggle_minigame");
    public static final Identifier HIDE_MINIGAME = Overgeared.id("hide_minigame");
    public static final Identifier RESET_MINIGAME = Overgeared.id("reset_minigame");
    public static final Identifier ONLY_RESET_MINIGAME = Overgeared.id("only_reset_minigame");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(KNAPPING_CHIP, (server, player, handler, buf, responseSender) ->
                KnappingChipC2SPacket.handle(KnappingChipC2SPacket.decode(buf), server, player));

        ServerPlayNetworking.registerGlobalReceiver(SELECT_TOOL_TYPE, (server, player, handler, buf, responseSender) ->
                SelectToolTypeC2SPacket.handle(SelectToolTypeC2SPacket.decode(buf), server, player));

        ServerPlayNetworking.registerGlobalReceiver(SEND_COUNTER, (server, player, handler, buf, responseSender) ->
                PacketSendCounterC2SPacket.handle(PacketSendCounterC2SPacket.decode(buf), server, player));

        ServerPlayNetworking.registerGlobalReceiver(SET_MINIGAME_VISIBLE, (server, player, handler, buf, responseSender) ->
                SetMinigameVisibleC2SPacket.handle(SetMinigameVisibleC2SPacket.decode(buf), server, player));

        ServerPlayNetworking.registerGlobalReceiver(MINIGAME_SET_STARTED, (server, player, handler, buf, responseSender) ->
                MinigameSetStartedC2SPacket.handle(MinigameSetStartedC2SPacket.decode(buf), server, player));
    }

    // sendToServer intentionally lives in the client package (ClientModMessages), not here -
    // ClientPlayNetworking is a client-only class, and this class is loaded on both sides.

    public static void sendToPlayer(Identifier channel, PacketByteBuf buf, ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, channel, buf);
    }

    public static void sendToAll(Identifier channel, PacketByteBuf buf, MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, channel, buf);
        }
    }

    public static PacketByteBuf buf() {
        return PacketByteBufs.create();
    }
}
