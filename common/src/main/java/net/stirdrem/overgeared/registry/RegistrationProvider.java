package net.stirdrem.overgeared.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.stirdrem.overgeared.Constants;
import net.stirdrem.overgeared.platform.Services;

import java.util.function.Supplier;
import java.util.Collection;

/**
 * Loader-agnostic registration for one registry, e.g.
 * <pre>{@code
 * static final RegistrationProvider<Item> ITEMS = RegistrationProvider.get(Registries.ITEM);
 * public static final RegistryEntry<Item> STEEL_INGOT = ITEMS.register("steel_ingot", () -> new Item(...));
 * }</pre>
 * Forge backs this with a {@code DeferredRegister} attached to the mod bus; Fabric registers immediately.
 * The owning class must be class-loaded from the loader entrypoint (Forge: mod constructor, Fabric: onInitialize).
 */
public interface RegistrationProvider<T> {

    static <T> RegistrationProvider<T> get(ResourceKey<? extends Registry<T>> registryKey) {
        return get(registryKey, Constants.MOD_ID);
    }

    static <T> RegistrationProvider<T> get(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        return Factory.INSTANCE.create(registryKey, modId);
    }

    <I extends T> RegistryEntry<I> register(String name, Supplier<? extends I> supplier);

    /** Everything registered through this provider, in registration order. */
    Collection<RegistryEntry<? extends T>> getEntries();

    /** Implemented per loader and found through {@link java.util.ServiceLoader}. */
    interface Factory {
        Factory INSTANCE = Services.load(Factory.class);

        <T> RegistrationProvider<T> create(ResourceKey<? extends Registry<T>> registryKey, String modId);
    }
}
