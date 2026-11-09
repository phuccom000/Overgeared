package net.stirdrem.overgeared.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;

public class ModEntities {

    // Same tracking settings as the old FabricEntityTypeBuilder#trackable(4, 20, true) and the
    // vanilla arrow entity type (range 4 chunks, update interval 20 ticks, 0.5x0.5 hitbox).
    public static final EntityType<LingeringArrowEntity> LINGERING_ARROW = register("lingering_arrow",
            EntityType.Builder.<LingeringArrowEntity>of(LingeringArrowEntity::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.5f, 0.5f)
                    .eyeHeight(0.13f)
                    .clientTrackingRange(4)
                    .updateInterval(20));

    public static final EntityType<UpgradeArrowEntity> UPGRADE_ARROW = register("upgrade_arrow",
            EntityType.Builder.<UpgradeArrowEntity>of(UpgradeArrowEntity::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.5f, 0.5f)
                    .eyeHeight(0.13f)
                    .clientTrackingRange(4)
                    .updateInterval(20));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Overgeared.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void register() {
    }
}
