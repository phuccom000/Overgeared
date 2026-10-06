package net.stirdrem.overgeared.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.stirdrem.overgeared.Overgeared;

import java.util.concurrent.CompletableFuture;

public class ModPoiTagProvider extends TagsProvider<PoiType> {
    public ModPoiTagProvider(PackOutput output,
                             CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, Registries.POINT_OF_INTEREST_TYPE, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        this.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE)
                .addOptional(ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "soundpoi")));
    }
}
