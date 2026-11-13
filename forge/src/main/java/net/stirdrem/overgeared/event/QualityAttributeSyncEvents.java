package net.stirdrem.overgeared.event;

import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.SyncQualityAttributesS2CPacket;

/** Quality attribute definitions are server data; clients need them for item tooltips (join and /reload). */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QualityAttributeSyncEvents {

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            ModMessages.sendToPlayer(new SyncQualityAttributesS2CPacket(), event.getPlayer());
        } else {
            ModMessages.sendToAll(new SyncQualityAttributesS2CPacket());
        }
    }
}
