package net.stirdrem.overgeared.event;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.stirdrem.overgeared.datapack.*;

public class ReloadListenerRegistry {

    public static void register() {
        ResourceManagerHelper helper = ResourceManagerHelper.get(PackType.SERVER_DATA);
        helper.registerReloadListener(new FabricBlueprintTooltypesReloadListener());
        helper.registerReloadListener(new FabricBreakSystemBlacklistReloadListener());
        helper.registerReloadListener(new FabricGrindingBlacklistReloadListener());
        helper.registerReloadListener(new FabricDurabilityBlacklistReloadListener());
        helper.registerReloadListener(new FabricCastingToolTypesReloadListener());
        helper.registerReloadListener(new FabricMaterialSettingsReloadListener());
        helper.registerReloadListener(new FabricKnappingResourceReloadListener());
        helper.registerReloadListener(new FabricRockInteractionReloadListener());
        helper.registerReloadListener(new FabricQualityAttributeReloadListener());
        // BreakSystemBlacklistReloadListener is intentionally not registered here,
        // matching upstream (see its own file for why).
    }
}
