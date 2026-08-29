package net.stirdrem.overgeared.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;

public class ModEntities {

    public static final EntityType<LingeringArrowEntity> LINGERING_ARROW = register("lingering_arrow",
            FabricEntityTypeBuilder.<LingeringArrowEntity>create(SpawnGroup.MISC, LingeringArrowEntity::new)
                    .trackable(4, 20, true)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .build());

    public static final EntityType<UpgradeArrowEntity> UPGRADE_ARROW = register("upgrade_arrow",
            FabricEntityTypeBuilder.<UpgradeArrowEntity>create(SpawnGroup.MISC, UpgradeArrowEntity::new)
                    .trackable(4, 20, true)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .build());

    private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String name, EntityType<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, Overgeared.id(name), type);
    }

    public static void register() {
    }
}
