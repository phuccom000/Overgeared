package net.stirdrem.overgeared.datapack;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.Overgeared;

public class FabricCastingToolTypesReloadListener extends CastingToolTypesReloadListener implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return Overgeared.id("casting_tooltypes_listener");
    }
}
