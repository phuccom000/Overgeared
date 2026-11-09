package net.stirdrem.overgeared.entity.custom;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//TODO: there's tons of duplicate code between this and UpgradeArrowEntity
public class LingeringArrowEntity extends AbstractArrow {
    private static final EntityDataAccessor<Integer> DATA_POTION_COLOR =
            SynchedEntityData.defineId(LingeringArrowEntity.class, EntityDataSerializers.INT);
    private final ItemStack referenceStack;
    private boolean cloudSpawned = false;

    public LingeringArrowEntity(EntityType<? extends LingeringArrowEntity> type, Level level, LivingEntity shooter, ItemStack stack, @Nullable ItemStack firedFromWeapon) {
        super(type, shooter, level, stack, firedFromWeapon);
        this.referenceStack = stack;
        PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        int color = potionContents.getColor();
        this.entityData.set(DATA_POTION_COLOR, color);
    }

    public LingeringArrowEntity(EntityType<? extends LingeringArrowEntity> type, Level level) {
        super(type, level);
        this.referenceStack = ItemStack.EMPTY;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_POTION_COLOR, -1); // Default no color
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PotionColor", this.entityData.get(DATA_POTION_COLOR));
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        tag.getInt("PotionColor").ifPresent(color -> this.entityData.set(DATA_POTION_COLOR, color));
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return referenceStack;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide() && !this.cloudSpawned && result.getType() != HitResult.Type.MISS) {
            PotionContents potionContents = this.referenceStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

            if (!potionContents.getAllEffects().iterator().hasNext()) {
                return;
            }

            this.cloudSpawned = true;
            makeAreaOfEffectCloud(this.referenceStack, potionContents, result);
        }
    }


    private void makeAreaOfEffectCloud(ItemStack stack, PotionContents potionContents,
                                       HitResult result) {
        Vec3 hit = result.getLocation();

        // Compute vertical motion ratio
        Vec3 motion = this.getDeltaMovement();
        double verticalRatio = motion.y / motion.length(); // -1 to 1

        // Map verticalRatio to offset: more vertical ➜ larger downward offset
        double offset = verticalRatio > 0 ? -verticalRatio * 0.5 : -0.2;

        double cloudY = hit.y + offset + 0.25;
        double cloudX = hit.x;
        double cloudZ = hit.z;

        AreaEffectCloud cloud = new AreaEffectCloud(level(), cloudX, cloudY, cloudZ);
        Entity owner = getOwner();
        if (owner instanceof LivingEntity le) {
            cloud.setOwner(le);
        }

        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());

        // Create reduced-duration potion contents for the cloud
        List<MobEffectInstance> reducedEffects = new java.util.ArrayList<>();
        for (MobEffectInstance inst : potionContents.getAllEffects()) {
            MobEffectInstance reducedEffect = new MobEffectInstance(
                    inst.getEffect(),
                    Math.max(inst.getDuration() / 8, 1), // 1/8 duration
                    inst.getAmplifier(),
                    inst.isAmbient(),
                    inst.isVisible(),
                    inst.showIcon()
            );
            reducedEffects.add(reducedEffect);
        }

        // Create new PotionContents with reduced effects
        PotionContents cloudContents = new PotionContents(
                potionContents.potion(),
                potionContents.customColor(),
                reducedEffects,
                potionContents.customName()
        );

        cloud.setPotionContents(cloudContents);

        level().addFreshEntity(cloud);
    }

    private void makeParticle(int amount) {
        int color = this.entityData.get(DATA_POTION_COLOR);
        if (color != -1 && amount > 0) {
            for (int j = 0; j < amount; ++j) {
                this.level().addParticle(
                        ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color),
                        this.getRandomX(0.5D),
                        this.getRandomY(),
                        this.getRandomZ(0.5D),
                        0.0D, 0.0D, 0.0D
                );
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.isInGround()) {
                if (this.inGroundTime % 5 == 0) {
                    this.makeParticle(1);
                }
            } else {
                this.makeParticle(2);
            }
        }
    }
}