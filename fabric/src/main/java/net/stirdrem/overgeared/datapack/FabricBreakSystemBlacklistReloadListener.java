package net.stirdrem.overgeared.datapack;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.Overgeared;

public class FabricBreakSystemBlacklistReloadListener extends BreakSystemBlacklistReloadListener implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return Overgeared.id("break_system_blacklist_listener");
    }
}
