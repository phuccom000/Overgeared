package net.stirdrem.overgeared.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.datapack.*;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ReloadListenerRegistry {
    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        event.addListener(new BlueprintTooltypesReloadListener());
        event.addListener(new GrindingBlacklistReloadListener());
        event.addListener(new DurabilityBlacklistReloadListener());
        event.addListener(new CastingToolTypesReloadListener());
        event.addListener(new MaterialSettingsReloadListener());
        event.addListener(new KnappingResourceReloadListener());
    }
}
