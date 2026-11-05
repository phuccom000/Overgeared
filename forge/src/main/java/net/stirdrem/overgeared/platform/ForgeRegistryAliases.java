package net.stirdrem.overgeared.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry aliases on Forge. A world's saved registry ids go through {@link MissingMappingsEvent}, where old ids are
 * remapped (which also adds an alias); the alias added at registration covers lookups by name, such as structures.
 */
public final class ForgeRegistryAliases {
    private static final Map<ResourceKey<? extends Registry<?>>, Map<ResourceLocation, ResourceLocation>> ALIASES = new HashMap<>();

    private ForgeRegistryAliases() {
    }

    public static void add(ResourceKey<? extends Registry<?>> registry, ResourceLocation oldId, ResourceLocation newId) {
        ALIASES.computeIfAbsent(registry, key -> new HashMap<>()).put(oldId, newId);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ForgeRegistryAliases::onRegister);
        MinecraftForge.EVENT_BUS.addListener(ForgeRegistryAliases::onMissingMappings);
    }

    private static void onRegister(RegisterEvent event) {
        Map<ResourceLocation, ResourceLocation> aliases = ALIASES.get(event.getRegistryKey());
        if (aliases != null && event.getForgeRegistry() instanceof ForgeRegistry<?> registry) {
            aliases.forEach(registry::addAlias);
        }
    }

    private static void onMissingMappings(MissingMappingsEvent event) {
        Map<ResourceLocation, ResourceLocation> aliases = ALIASES.get(event.getKey());
        if (aliases != null) {
            remap(event, event.getKey(), aliases);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void remap(MissingMappingsEvent event, ResourceKey<? extends Registry<?>> key,
                                  Map<ResourceLocation, ResourceLocation> aliases) {
        ResourceKey<? extends Registry<T>> registryKey = (ResourceKey<? extends Registry<T>>) key;
        // The event's registries are the world's snapshot, which lacks the new ids; look them up in the live registry.
        // Defaulted registries (blocks, items) return air for unknown ids, so check the id exists first.
        ForgeRegistry<T> active = RegistryManager.ACTIVE.getRegistry(registryKey);
        if (active == null) return;
        for (MissingMappingsEvent.Mapping<T> mapping : event.getAllMappings(registryKey)) {
            ResourceLocation newId = aliases.get(mapping.getKey());
            if (newId != null && active.containsKey(newId)) {
                mapping.remap(active.getValue(newId));
            }
        }
    }
}
