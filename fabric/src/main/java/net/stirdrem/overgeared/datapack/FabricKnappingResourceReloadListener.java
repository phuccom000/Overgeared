package net.stirdrem.overgeared.datapack;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.Overgeared;

public class FabricKnappingResourceReloadListener extends KnappingResourceReloadListener implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return Overgeared.id("knapping_resources_listener");
    }
}
