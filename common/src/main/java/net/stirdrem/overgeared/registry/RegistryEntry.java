package net.stirdrem.overgeared.registry;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * A registered object, resolved lazily. Mirrors the parts of Forge's {@code RegistryObject} the mod uses,
 * so common code can call {@code ModItems.X.get()} / {@code getId()} on either loader.
 */
public interface RegistryEntry<T> extends Supplier<T> {

    /** The registered object. Only valid once registration has run. */
    @Override
    T get();

    ResourceLocation getId();
}
