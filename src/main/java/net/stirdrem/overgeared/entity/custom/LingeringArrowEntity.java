package net.stirdrem.overgeared.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.stirdrem.overgeared.util.PotionColorHelper;

import java.util.List;

public class LingeringArrowEntity extends ArrowEntity {
    private static final TrackedData<Integer> DATA_POTION_COLOR =
            DataTracker.registerData(LingeringArrowEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private final ItemStack referenceStack;

    public LingeringArrowEntity(World world, LivingEntity shooter, ItemStack stack) {
        super(world, shooter);
        this.referenceStack = stack;
        int color = PotionColorHelper.getColor(stack);
        this.dataTracker.set(DATA_POTION_COLOR, color);
    }

    public LingeringArrowEntity(EntityType<? extends ArrowEntity> type, World world) {
        super(type, world);
        this.referenceStack = ItemStack.EMPTY;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DATA_POTION_COLOR, -1); // Default no color
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        tag.putInt("PotionColor", this.dataTracker.get(DATA_POTION_COLOR));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        if (tag.contains("PotionColor", NbtElement.NUMBER_TYPE)) {
            this.dataTracker.set(DATA_POTION_COLOR, tag.getInt("PotionColor"));
        }
    }

    @Override
    protected void onCollision(HitResult result) {
        super.onCollision(result);
        if (!getWorld().isClient) {
            ItemStack stack = this.referenceStack;
            Potion potion = PotionColorHelper.getPotion(stack.getNbt());
            List<StatusEffectInstance> effects = PotionColorHelper.getAllEffects(stack.getNbt());

            if (!effects.isEmpty()) {
                makeAreaOfEffectCloud(stack, effects, result);
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult result) {
        super.onBlockHit(result);
        if (!getWorld().isClient) {
            ItemStack stack = this.referenceStack;
            List<StatusEffectInstance> effects = PotionColorHelper.getMobEffects(stack.getNbt());

            if (!effects.isEmpty()) {
                makeAreaOfEffectCloud(stack, effects, result);
            }
        }
    }

    private void makeAreaOfEffectCloud(ItemStack stack, List<StatusEffectInstance> effects, HitResult result) {
        Vec3d hit = result.getPos();

        // Compute vertical motion ratio
        Vec3d motion = this.getVelocity();
        double verticalRatio = motion.y / motion.length(); // -1 to 1

        // Map verticalRatio to offset: more vertical ➜ larger downward offset
        double offset = verticalRatio > 0 ? -verticalRatio * 0.5 : -0.2;

        double cloudY = hit.y + offset + 0.25;
        double cloudX = hit.x;
        double cloudZ = hit.z;

        AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(getWorld(), cloudX, cloudY, cloudZ);
        Entity owner = getOwner();
        if (owner instanceof LivingEntity le) {
            cloud.setOwner(le);
        }

        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusGrowth(-cloud.getRadius() / cloud.getDuration());
        cloud.setPotion(PotionColorHelper.getPotion(stack.getNbt()));

        for (StatusEffectInstance inst : effects) {
            StatusEffectInstance reducedEffect = new StatusEffectInstance(
                    inst.getEffectType(),
                    Math.max(inst.getDuration() / 8, 1), // 1/4 duration
                    inst.getAmplifier(),
                    inst.isAmbient(),
                    inst.shouldShowParticles(),
                    inst.shouldShowIcon()
            );
            cloud.addEffect(reducedEffect);
        }

        NbtCompound compoundtag = stack.getNbt();
        if (compoundtag != null && compoundtag.contains("CustomPotionColor", NbtElement.NUMBER_TYPE)) {
            cloud.setColor(compoundtag.getInt("CustomPotionColor"));
        }

        ((net.minecraft.server.world.ServerWorld) getWorld()).spawnEntity(cloud);
    }

    private void makeParticle(int amount) {
        int color = this.dataTracker.get(DATA_POTION_COLOR);
        if (color != -1 && amount > 0) {
            double r = (double) (color >> 16 & 255) / 255.0D;
            double g = (double) (color >> 8 & 255) / 255.0D;
            double b = (double) (color & 255) / 255.0D;

            for (int j = 0; j < amount; ++j) {
                this.getWorld().addParticle(ParticleTypes.ENTITY_EFFECT, this.getParticleX(0.5D), this.getRandomBodyY(), this.getParticleZ(0.5D), r, g, b);
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
