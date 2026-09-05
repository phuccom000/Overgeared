package net.stirdrem.overgeared.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.util.PotionColorHelper;

import java.util.List;

public class LingeringArrowEntity extends Arrow {
    private static final EntityDataAccessor<Integer> DATA_POTION_COLOR =
            SynchedEntityData.defineId(LingeringArrowEntity.class, EntityDataSerializers.INT);
    private final ItemStack referenceStack;

    public LingeringArrowEntity(Level world, LivingEntity shooter, ItemStack stack) {
        super(world, shooter);
        this.referenceStack = stack;
        int color = PotionColorHelper.getColor(stack);
        this.entityData.set(DATA_POTION_COLOR, color);
    }

    public LingeringArrowEntity(EntityType<? extends Arrow> type, Level world) {
        super(type, world);
        this.referenceStack = ItemStack.EMPTY;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_POTION_COLOR, -1); // Default no color
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PotionColor", this.entityData.get(DATA_POTION_COLOR));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PotionColor", Tag.TAG_ANY_NUMERIC)) {
            this.entityData.set(DATA_POTION_COLOR, tag.getInt("PotionColor"));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            ItemStack stack = this.referenceStack;
            Potion potion = PotionColorHelper.getPotion(stack.getTag());
            List<MobEffectInstance> effects = PotionColorHelper.getAllEffects(stack.getTag());

            if (!effects.isEmpty()) {
                makeAreaOfEffectCloud(stack, effects, result);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) {
            ItemStack stack = this.referenceStack;
            List<MobEffectInstance> effects = PotionColorHelper.getMobEffects(stack.getTag());

            if (!effects.isEmpty()) {
                makeAreaOfEffectCloud(stack, effects, result);
            }
        }
    }

    private void makeAreaOfEffectCloud(ItemStack stack, List<MobEffectInstance> effects, HitResult result) {
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
        cloud.setPotion(PotionColorHelper.getPotion(stack.getTag()));

        for (MobEffectInstance inst : effects) {
            MobEffectInstance reducedEffect = new MobEffectInstance(
                    inst.getEffect(),
                    Math.max(inst.getDuration() / 8, 1), // 1/4 duration
                    inst.getAmplifier(),
                    inst.isAmbient(),
                    inst.isVisible(),
                    inst.showIcon()
            );
            cloud.addEffect(reducedEffect);
        }

        CompoundTag compoundtag = stack.getTag();
        if (compoundtag != null && compoundtag.contains("CustomPotionColor", Tag.TAG_ANY_NUMERIC)) {
            cloud.setFixedColor(compoundtag.getInt("CustomPotionColor"));
        }

        ((net.minecraft.server.level.ServerLevel) level()).addFreshEntity(cloud);
    }

    private void makeParticle(int amount) {
        int color = this.entityData.get(DATA_POTION_COLOR);
        if (color != -1 && amount > 0) {
            double r = (double) (color >> 16 & 255) / 255.0D;
            double g = (double) (color >> 8 & 255) / 255.0D;
            double b = (double) (color & 255) / 255.0D;

            for (int j = 0; j < amount; ++j) {
                this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), r, g, b);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.inGround) {
            if (this.inGroundTime % 5 == 0) {
                this.makeParticle(1);
            }
        } else {
            this.makeParticle(2);
        }
    }

}
