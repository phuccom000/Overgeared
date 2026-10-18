package net.stirdrem.overgeared.datapack;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.item.ModItems;

public class FabricRockInteractionReloadListener extends RockInteractionReloadListener implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return Overgeared.id("rock_interactions_listener");
    }

    @Override
    protected Item getRock() {
        return ModItems.ROCK;
    }
}
