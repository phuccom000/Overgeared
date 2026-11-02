package net.stirdrem.overgeared.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import net.stirdrem.overgeared.platform.Services;
import net.stirdrem.overgeared.registry.RegistrationProvider;
import net.stirdrem.overgeared.registry.RegistryEntry;

public class ModEntities {
    private static final RegistrationProvider<EntityType<?>> ENTITY_TYPES = RegistrationProvider.get(Registries.ENTITY_TYPE);

    public static final RegistryEntry<EntityType<LingeringArrowEntity>> LINGERING_ARROW =
            ENTITY_TYPES.register("lingering_arrow", () -> Services.PLATFORM.createEntityType(
                    "lingering_arrow", LingeringArrowEntity::new, MobCategory.MISC, 0.5f, 0.5f, 4, 20));

    public static final RegistryEntry<EntityType<UpgradeArrowEntity>> UPGRADE_ARROW =
            ENTITY_TYPES.register("upgrade_arrow", () -> Services.PLATFORM.createEntityType(
                    "upgrade_arrow", UpgradeArrowEntity::new, MobCategory.MISC, 0.5f, 0.5f, 4, 20));

    /** Class-loads this registry. Call from the loader entrypoint. */
    public static void init() {
    }
}
