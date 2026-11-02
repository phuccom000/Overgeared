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

/** Forge implementation: one {@link DeferredRegister} per registry, attached to the mod event bus on creation. */
public class ForgeRegistrationFactory implements RegistrationProvider.Factory {

    @Override
    public <T> RegistrationProvider<T> create(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        DeferredRegister<T> register = DeferredRegister.create(registryKey, modId);
        // Registry classes are class-loaded from the mod constructor, so the loading context is valid here
        register.register(FMLJavaModLoadingContext.get().getModEventBus());
        return new Provider<>(register);
    }

    private record Provider<T>(DeferredRegister<T> register) implements RegistrationProvider<T> {
        @Override
        public <I extends T> RegistryEntry<I> register(String name, Supplier<? extends I> supplier) {
            RegistryObject<I> object = register.register(name, supplier);
            return new Entry<>(object);
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
