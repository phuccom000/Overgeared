package net.stirdrem.overgeared.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.stirdrem.overgeared.platform.Services;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A loader-independent deferred register. Entries are collected when the holder fields are
 * initialised and handed to the loader by {@link #register()}, which registers them at the
 * appropriate time (immediately on Fabric, during {@code RegisterEvent} on NeoForge).
 */
public class DeferredRegister<R> {
    private final ResourceKey<? extends Registry<R>> registryKey;
    private final String namespace;
    private final Map<DeferredHolder<R, ?>, Supplier<? extends R>> entries = new LinkedHashMap<>();
    private boolean registered;

    protected DeferredRegister(ResourceKey<? extends Registry<R>> registryKey, String namespace) {
        this.registryKey = registryKey;
        this.namespace = namespace;
    }

    public static <R> DeferredRegister<R> create(ResourceKey<? extends Registry<R>> registryKey, String namespace) {
        return new DeferredRegister<>(registryKey, namespace);
    }

    public static Items createItems(String namespace) {
        return new Items(namespace);
    }

    public static Blocks createBlocks(String namespace) {
        return new Blocks(namespace);
    }

    public <T extends R> DeferredHolder<R, T> register(String name, Supplier<? extends T> supplier) {
        return register(name, id -> supplier.get());
    }

    public <T extends R> DeferredHolder<R, T> register(String name, Function<ResourceLocation, ? extends T> factory) {
        if (registered) {
            throw new IllegalStateException("Cannot add entry " + name + " to " + registryKey.location() + " after it was registered");
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, name);
        DeferredHolder<R, T> holder = createHolder(ResourceKey.create(registryKey, id));
        if (entries.putIfAbsent(holder, () -> factory.apply(id)) != null) {
            throw new IllegalArgumentException("Duplicate registration " + id);
        }
        return holder;
    }

    protected <T extends R> DeferredHolder<R, T> createHolder(ResourceKey<R> key) {
        return new DeferredHolder<>(key);
    }

    /** Hands the collected entries to the loader. Call once, during mod construction. */
    public void register() {
        if (registered) {
            throw new IllegalStateException("Register " + registryKey.location() + " was already registered");
        }
        registered = true;
        Services.PLATFORM.register(this);
    }

    public ResourceKey<? extends Registry<R>> getRegistryKey() {
        return registryKey;
    }

    public String getNamespace() {
        return namespace;
    }

    /** All holders, in registration order. */
    public Collection<DeferredHolder<R, ?>> getEntries() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    /** Holders together with the factories creating their values. For use by the loader implementation. */
    public Map<DeferredHolder<R, ?>, Supplier<? extends R>> getEntriesWithFactories() {
        return Collections.unmodifiableMap(entries);
    }

    public static class Items extends DeferredRegister<Item> {
        protected Items(String namespace) {
            super(Registries.ITEM, namespace);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Item> DeferredItem<T> register(String name, Supplier<? extends T> supplier) {
            return (DeferredItem<T>) super.register(name, supplier);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Item> DeferredItem<T> register(String name, Function<ResourceLocation, ? extends T> factory) {
            return (DeferredItem<T>) super.register(name, factory);
        }

        public <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, ? extends T> factory, Item.Properties properties) {
            return register(name, () -> factory.apply(properties));
        }

        public <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, ? extends T> factory) {
            return registerItem(name, factory, new Item.Properties());
        }

        public DeferredItem<Item> registerSimpleItem(String name, Item.Properties properties) {
            return registerItem(name, Item::new, properties);
        }

        public DeferredItem<Item> registerSimpleItem(String name) {
            return registerSimpleItem(name, new Item.Properties());
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, Supplier<? extends Block> block, Item.Properties properties) {
            return register(name, () -> new BlockItem(block.get(), properties));
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(DeferredBlock<?> block) {
            return registerSimpleBlockItem(block.getId().getPath(), block, new Item.Properties());
        }

        @Override
        protected <T extends Item> DeferredItem<T> createHolder(ResourceKey<Item> key) {
            return new DeferredItem<>(key);
        }
    }

    public static class Blocks extends DeferredRegister<Block> {
        protected Blocks(String namespace) {
            super(Registries.BLOCK, namespace);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Block> DeferredBlock<T> register(String name, Supplier<? extends T> supplier) {
            return (DeferredBlock<T>) super.register(name, supplier);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Block> DeferredBlock<T> register(String name, Function<ResourceLocation, ? extends T> factory) {
            return (DeferredBlock<T>) super.register(name, factory);
        }

        @Override
        protected <T extends Block> DeferredBlock<T> createHolder(ResourceKey<Block> key) {
            return new DeferredBlock<>(key);
        }
    }
}
