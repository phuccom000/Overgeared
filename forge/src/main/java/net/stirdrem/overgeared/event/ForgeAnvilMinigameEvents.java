package net.stirdrem.overgeared.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.SetMinigameVisibleC2SPacket;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Overgeared.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeAnvilMinigameEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        //ensureInitialized();
        Minecraft mc = Minecraft.getInstance();
        AnvilMinigameEvents.onClientTick(mc);
    }

    public static void setIsVisible(BlockPos pos, boolean isVisible) {
        ClientAnvilMinigameData.isVisible = isVisible;
        ModMessages.sendToServer(new SetMinigameVisibleC2SPacket(pos, isVisible));
    }


    public static void hideMinigame(UUID playerId) {
        ClientAnvilMinigameData.isVisible = false;
        BlockPos pos = ClientAnvilMinigameData.playerAnvilPositions.get(playerId);
        if (pos != null && !pos.equals(BlockPos.ZERO)) {
            ModMessages.sendToServer(new SetMinigameVisibleC2SPacket(pos, false));
        }
    }

}