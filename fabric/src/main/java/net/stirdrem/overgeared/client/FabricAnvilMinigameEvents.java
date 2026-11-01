package net.stirdrem.overgeared.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.SetMinigameVisibleC2SPacket;

import java.util.UUID;

public class FabricAnvilMinigameEvents {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(AnvilMinigameEvents::onClientTick);
    }


    public static void setIsVisible(BlockPos pos, boolean isVisible) {
        ClientAnvilMinigameData.isVisible = isVisible;
        FriendlyByteBuf buf = ModMessages.buf();
        SetMinigameVisibleC2SPacket.encode(new SetMinigameVisibleC2SPacket(pos, isVisible), buf);
        ClientModMessages.sendToServer(ModMessages.SET_MINIGAME_VISIBLE, buf);
    }


    public static void hideMinigame(UUID playerId) {
        ClientAnvilMinigameData.isVisible = false;
        BlockPos pos = ClientAnvilMinigameData.playerAnvilPositions.get(playerId);
        if (pos != null && !pos.equals(BlockPos.ZERO)) {
            FriendlyByteBuf buf = ModMessages.buf();
            SetMinigameVisibleC2SPacket.encode(new SetMinigameVisibleC2SPacket(pos, false), buf);
            ClientModMessages.sendToServer(ModMessages.SET_MINIGAME_VISIBLE, buf);
        }
    }

}
