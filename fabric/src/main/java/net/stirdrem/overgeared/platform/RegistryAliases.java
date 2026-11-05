package net.stirdrem.overgeared.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry aliases for Fabric, which has no built-in equivalent of Forge's registry aliases.
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

    /** The id {@code id} was renamed to, or null. Registry lookups pass null ids, which have no alias. */
    @Nullable
    public static ResourceLocation get(ResourceKey<? extends Registry<?>> registry, @Nullable ResourceLocation id) {
        if (id == null) return null;
        Map<ResourceLocation, ResourceLocation> map = ALIASES.get(registry);
        return map == null ? null : map.get(id);
    }
}
