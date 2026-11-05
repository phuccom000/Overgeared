package net.stirdrem.overgeared.platform;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.registry.RegistrationProvider;
import net.stirdrem.overgeared.registry.RegistryEntry;

import java.util.function.Supplier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Fabric implementation: registers immediately, so entries are resolved as soon as they're created. */
public class FabricRegistrationFactory implements RegistrationProvider.Factory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> RegistrationProvider<T> create(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
        if (registry == null) {
            throw new IllegalArgumentException("Unknown registry " + registryKey.location());
        }
        return new Provider<>(registry, modId, new ArrayList<>());
    }

    private record Provider<T>(Registry<T> registry, String modId, List<RegistryEntry<? extends T>> entries)
            implements RegistrationProvider<T> {
        @Override
        public <I extends T> RegistryEntry<I> register(String name, Supplier<? extends I> supplier) {
            ResourceLocation id = new ResourceLocation(modId, name);
            I value = Registry.register(registry, id, supplier.get());
            Entry<I> entry = new Entry<>(value, id);
            entries.add(entry);
            return entry;
        }

        @Override
        public Collection<RegistryEntry<? extends T>> getEntries() {
            return Collections.unmodifiableList(entries);
        }
    }

    private record Entry<I>(I value, ResourceLocation id) implements RegistryEntry<I> {
        @Override
        public I get() {
            return value;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }
    }
}
