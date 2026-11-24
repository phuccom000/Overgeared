package net.stirdrem.overgeared.entity.custom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LingeringArrowEntity extends PotionArrowEntity {

    public LingeringArrowEntity(EntityType<? extends LingeringArrowEntity> type, Level level, LivingEntity shooter, ItemStack stack, @Nullable ItemStack firedFromWeapon) {
        super(type, level, shooter, stack, firedFromWeapon);
    }

    public LingeringArrowEntity(EntityType<? extends LingeringArrowEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected boolean shouldSpawnCloud() {
        return true;
    }
}
