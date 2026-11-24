package net.stirdrem.overgeared.entity.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class PotionArrowEntity extends AbstractArrow {
    protected static final EntityDataAccessor<Integer> DATA_POTION_COLOR =
            SynchedEntityData.defineId(PotionArrowEntity.class, EntityDataSerializers.INT);

    protected final ItemStack referenceStack;
    protected final PotionContents potionContents;
    private boolean cloudSpawned = false;

    protected PotionArrowEntity(EntityType<? extends PotionArrowEntity> type, Level level) {
        super(type, level);
        this.referenceStack = ItemStack.EMPTY;
        this.potionContents = PotionContents.EMPTY;
    }

    protected PotionArrowEntity(EntityType<? extends PotionArrowEntity> type, Level level, LivingEntity shooter, ItemStack stack, @Nullable ItemStack firedFromWeapon) {
        super(type, shooter, level, stack, firedFromWeapon);
        this.referenceStack = stack;
        this.potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        this.entityData.set(DATA_POTION_COLOR, colorOf(this.potionContents));
    }

    protected PotionArrowEntity(EntityType<? extends PotionArrowEntity> type, Level level, double x, double y, double z, ItemStack stack, @Nullable ItemStack firedFromWeapon) {
        super(type, x, y, z, level, stack, firedFromWeapon);
        this.referenceStack = stack;
        this.potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        this.entityData.set(DATA_POTION_COLOR, colorOf(this.potionContents));
    }

    private static int colorOf(PotionContents contents) {
        return contents.equals(PotionContents.EMPTY) ? -1 : contents.getColor();
    }

    protected abstract boolean shouldSpawnCloud();

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_POTION_COLOR, -1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PotionColor", this.entityData.get(DATA_POTION_COLOR));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PotionColor", 99)) {
            this.entityData.set(DATA_POTION_COLOR, tag.getInt("PotionColor"));
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return referenceStack;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide || cloudSpawned || result.getType() == HitResult.Type.MISS) return;
        if (!potionContents.getAllEffects().iterator().hasNext()) return;
        if (!shouldSpawnCloud()) return;

        cloudSpawned = true;
        makeAreaOfEffectCloud(result);
    }

    private void makeAreaOfEffectCloud(HitResult result) {
        Vec3 hit = result.getLocation();
        Vec3 motion = this.getDeltaMovement();
        double verticalRatio = motion.y / motion.length();
        // steeper downward shots sink the cloud further so it sits on the surface hit
        double offset = verticalRatio > 0 ? -verticalRatio * 0.5 : -0.2;

        AreaEffectCloud cloud = new AreaEffectCloud(level(), hit.x, hit.y + offset + 0.25, hit.z);
        if (getOwner() instanceof LivingEntity owner) {
            cloud.setOwner(owner);
        }
        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());

        List<MobEffectInstance> reduced = new ArrayList<>();
        for (MobEffectInstance inst : potionContents.getAllEffects()) {
            reduced.add(reducedDuration(inst));
        }
        cloud.setPotionContents(new PotionContents(potionContents.potion(), potionContents.customColor(), reduced));

        level().addFreshEntity(cloud);
    }

    protected static MobEffectInstance reducedDuration(MobEffectInstance inst) {
        return new MobEffectInstance(inst.getEffect(), Math.max(inst.getDuration() / 8, 1),
                inst.getAmplifier(), inst.isAmbient(), inst.isVisible(), inst.showIcon());
    }

    private void makeParticle(int amount) {
        int color = this.entityData.get(DATA_POTION_COLOR);
        if (color == -1) return;
        for (int j = 0; j < amount; ++j) {
            this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color),
                    this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) return;
        if (!this.inGround) {
            this.makeParticle(2);
        } else if (this.inGroundTime % 5 == 0) {
            this.makeParticle(1);
        }
    }
}
