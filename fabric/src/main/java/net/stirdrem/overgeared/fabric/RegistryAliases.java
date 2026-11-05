package net.stirdrem.overgeared.fabric;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry aliases for Fabric, which has no built-in equivalent of NeoForge's {@code Registry#addAlias}.
 * Looked up by {@code MappedRegistryAliasMixin} only when a registry lookup misses.
 */
public final class RegistryAliases {
    private static final Map<ResourceKey<? extends Registry<?>>, Map<ResourceLocation, ResourceLocation>> ALIASES =
            new ConcurrentHashMap<>();

    private RegistryAliases() {
    }

    public static void add(ResourceKey<? extends Registry<?>> registry, ResourceLocation oldId, ResourceLocation newId) {
        ALIASES.computeIfAbsent(registry, k -> new ConcurrentHashMap<>()).put(oldId, newId);
    }

    @Nullable
    public static ResourceLocation get(ResourceKey<? extends Registry<?>> registry, ResourceLocation id) {
        Map<ResourceLocation, ResourceLocation> map = ALIASES.get(registry);
        return map == null ? null : map.get(id);
    }
}
