package net.stirdrem.overgeared.event;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.stirdrem.overgeared.datapack.*;

public class ReloadListenerRegistry {

    public static void register() {
        ResourceManagerHelper helper = ResourceManagerHelper.get(ResourceType.SERVER_DATA);
        helper.registerReloadListener(new BlueprintTooltypesReloadListener());
        helper.registerReloadListener(new GrindingBlacklistReloadListener());
        helper.registerReloadListener(new DurabilityBlacklistReloadListener());
        helper.registerReloadListener(new CastingToolTypesReloadListener());
        helper.registerReloadListener(new MaterialSettingsReloadListener());
        helper.registerReloadListener(new KnappingResourceReloadListener());
        helper.registerReloadListener(new RockInteractionReloadListener());
        helper.registerReloadListener(new QualityAttributeReloadListener());
        // BreakSystemBlacklistReloadListener is intentionally not registered here,
        // matching upstream (see its own file for why).
    }
}
