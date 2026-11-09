package net.stirdrem.overgeared.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A lazily-resolved reference to a registry entry created by {@link DeferredRegister}.
 * <p>
 * Unlike NeoForge's class of the same name this does not implement {@link Holder} itself, because
 * vanilla compares some holders by identity. Use {@link #getDelegate()} where a real holder is needed.
 */
public class DeferredHolder<R, T extends R> implements Supplier<T> {
    protected final ResourceKey<R> key;
    private Holder<R> holder;

    protected DeferredHolder(ResourceKey<R> key) {
        this.key = Objects.requireNonNull(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) getDelegate().value();
    }

    public R value() {
        return getDelegate().value();
    }

    /** The holder the registry itself uses for this entry. */
    @SuppressWarnings("unchecked")
    public Holder<R> getDelegate() {
        if (holder == null) {
            Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.getValue(key.registry());
            if (registry == null) {
                throw new IllegalStateException("Registry " + key.registry() + " is not present");
            }
            holder = registry.get(key)
                    .orElseThrow(() -> new IllegalStateException("Registry entry " + key.identifier() + " is not registered yet"));
        }
        return holder;
    }

    public boolean isBound() {
        try {
            return getDelegate().isBound();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public Identifier getId() {
        return key.identifier();
    }

    public ResourceKey<R> getKey() {
        return key;
    }

    /** The id as a string, like {@code Holder#getRegisteredName}. */
    public String getRegisteredName() {
        return key.identifier().toString();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof DeferredHolder<?, ?> other && key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "DeferredHolder{" + key + "}";
    }
}
