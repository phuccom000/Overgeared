package net.stirdrem.overgeared.datapack;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;

/**
 * Base for the mod's datapack listeners, which read raw JSON from {@code data/<namespace>/<directory>/}.
 * <p>
 * Decoding tags (e.g. {@code "#minecraft:planks"} ingredients) needs registry access since 1.21.2;
 * the loaders hand the reload's registries to {@link #setRegistries} before the listeners run.
 */
public abstract class JsonDataReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
    private static volatile HolderLookup.Provider registries;

    protected JsonDataReloadListener(String directory) {
        super(ExtraCodecs.JSON, FileToIdConverter.json(directory));
    }

    public static void setRegistries(HolderLookup.Provider provider) {
        registries = provider;
    }

    /** JSON ops that can resolve registry elements and tags of the reload in progress. */
    protected static DynamicOps<JsonElement> jsonOps() {
        HolderLookup.Provider provider = registries;
        return provider != null ? provider.createSerializationContext(JsonOps.INSTANCE) : JsonOps.INSTANCE;
    }
}
