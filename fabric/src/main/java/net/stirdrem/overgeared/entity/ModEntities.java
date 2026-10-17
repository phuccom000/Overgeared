package net.stirdrem.overgeared.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;

public class ModEntities {

    public static final EntityType<LingeringArrowEntity> LINGERING_ARROW = register("lingering_arrow",
            FabricEntityTypeBuilder.<LingeringArrowEntity>create(MobCategory.MISC, LingeringArrowEntity::new)
                    .trackable(4, 20, true)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .build());

    public static final EntityType<UpgradeArrowEntity> UPGRADE_ARROW = register("upgrade_arrow",
            FabricEntityTypeBuilder.<UpgradeArrowEntity>create(MobCategory.MISC, UpgradeArrowEntity::new)
                    .trackable(4, 20, true)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .build());

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType<T> type) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, Overgeared.id(name), type);
    }

    public static void register() {
    }
}
