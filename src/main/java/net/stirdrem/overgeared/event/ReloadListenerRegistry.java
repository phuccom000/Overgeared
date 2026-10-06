package net.stirdrem.overgeared.event;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;
import net.stirdrem.overgeared.datapack.*;

public class ReloadListenerRegistry {

    public static void register() {
        ResourceLoader loader = ResourceLoader.get(PackType.SERVER_DATA);
        register(loader, new BlueprintTooltypesReloadListener());
        register(loader, GrindingBlacklistReloadListener.INSTANCE);
        register(loader, DurabilityBlacklistReloadListener.INSTANCE);
        register(loader, CastingToolTypesReloadListener.INSTANCE);
        register(loader, MaterialSettingsReloadListener.INSTANCE);
        register(loader, new KnappingResourceReloadListener());
        register(loader, RockInteractionReloadListener.INSTANCE);
        register(loader, QualityAttributeReloadListener.INSTANCE);
        // BreakSystemBlacklistReloadListener is intentionally not registered here,
        // matching upstream (see its own file for why).
    }

    private static void register(ResourceLoader loader, OvergearedJsonReloadListener listener) {
        loader.registerReloadListener(listener.getFabricId(), listener);
    }
}
