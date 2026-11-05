package net.stirdrem.overgeared.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.registry.RegistrationProvider;
import net.stirdrem.overgeared.registry.RegistryEntry;

import java.util.function.Supplier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Forge implementation: one {@link DeferredRegister} per registry, attached to the mod event bus on creation. */
public class ForgeRegistrationFactory implements RegistrationProvider.Factory {

    @Override
    public <T> RegistrationProvider<T> create(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        DeferredRegister<T> register = DeferredRegister.create(registryKey, modId);
        // Registry classes are class-loaded from the mod constructor, so the loading context is valid here
        register.register(FMLJavaModLoadingContext.get().getModEventBus());
        return new Provider<>(register, new ArrayList<>());
    }

    private record Provider<T>(DeferredRegister<T> register, List<RegistryEntry<? extends T>> entries)
            implements RegistrationProvider<T> {
        @Override
        public <I extends T> RegistryEntry<I> register(String name, Supplier<? extends I> supplier) {
            RegistryObject<I> object = register.register(name, supplier);
            Entry<I> entry = new Entry<>(object);
            entries.add(entry);
            return entry;
        }

        @Override
        public Collection<RegistryEntry<? extends T>> getEntries() {
            return Collections.unmodifiableList(entries);
        }
    }

    private record Entry<I>(RegistryObject<I> object) implements RegistryEntry<I> {
        @Override
        public I get() {
            return object.get();
        }

        @Override
        public ResourceLocation getId() {
            return object.getId();
        }
    }
}
