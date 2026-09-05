package net.stirdrem.overgeared.entity.custom;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.PotionColorHelper;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class UpgradeArrowEntity extends AbstractArrow {
    private static final EntityDataAccessor<Byte> DATA_TIER =
            SynchedEntityData.defineId(UpgradeArrowEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_POTION_COLOR =
            SynchedEntityData.defineId(UpgradeArrowEntity.class, EntityDataSerializers.INT);

    private final ItemStack referenceStack;
    private final Set<MobEffectInstance> effects = Sets.newHashSet();
    private Potion potion = Potions.EMPTY;

    public UpgradeArrowEntity(ArrowTier tier, Level world, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.UPGRADE_ARROW, shooter, world);
        this.referenceStack = stack;
        this.entityData.set(DATA_TIER, (byte) tier.ordinal());

        // Server-side only: extract potion color
        CompoundTag tag = stack.getTag();
        int color = -1;
        if (tag != null && (tag.contains("Potion") || tag.contains("CustomPotionEffects") || tag.contains("LingeringPotion")))
            color = PotionColorHelper.getColor(stack);
        this.entityData.set(DATA_POTION_COLOR, color);
        this.potion = getPotion(tag);
    }

    public UpgradeArrowEntity(EntityType<? extends AbstractArrow> type, Level world) {
        super(type, world);
        this.referenceStack = ItemStack.EMPTY;
    }

    public UpgradeArrowEntity(ArrowTier tier, Level world, double x, double y, double z, ItemStack stack) {
        super(ModEntities.UPGRADE_ARROW, x, y, z, world);
        this.referenceStack = stack;
        this.entityData.set(DATA_TIER, (byte) tier.ordinal());

        CompoundTag tag = stack.getTag();
        int color = -1;
        if (tag != null && (tag.contains("Potion") || tag.contains("CustomPotionEffects") || tag.contains("LingeringPotion"))) {
            color = PotionColorHelper.getColor(stack);
        }
        this.entityData.set(DATA_POTION_COLOR, color);
        this.potion = getPotion(tag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TIER, (byte) ArrowTier.FLINT.ordinal());
        this.entityData.define(DATA_POTION_COLOR, -1); // Default no color
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        setBaseDamage(getBaseDamage() * getArrowTier().getDamageBonus());
        super.onHitEntity(result);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        Entity owner = this.getOwner(); // More reliable than getEffectSource()
        if (owner == null) {
            owner = this; // Fallback to the arrow itself
        }
        for (MobEffectInstance effect : this.potion.getEffects()) {
            if (effect.getEffect().isInstantenous()) {
                effect.getEffect().applyInstantenousEffect(owner, owner instanceof LivingEntity livingOwner ? livingOwner : null,
                        target, effect.getAmplifier(), 1.0D);
            } else {
                MobEffectInstance reduced = new MobEffectInstance(
                        effect.getEffect(),
                        Math.max(effect.getDuration() / 8, 1),
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                );
                target.addEffect(reduced, owner);
            }
        }

        for (MobEffectInstance effect : this.effects) {
            if (effect.getEffect().isInstantenous()) {
                effect.getEffect().applyInstantenousEffect(owner, owner instanceof LivingEntity livingOwner ? livingOwner : null,
                        target, effect.getAmplifier(), 1.0D);
            } else {
                target.addEffect(effect, owner);
            }
        }
    }


    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            CompoundTag tag = this.referenceStack.getTag();

            // Only lingering type creates cloud
            if (tag != null && (tag.contains("LingeringPotion") || getArrowTier() == ArrowTier.FLINT && tag.contains("Potion", Tag.TAG_STRING))) {
                Potion potion = getPotion(tag);
                List<MobEffectInstance> effects = getAllEffects(tag);
                if (!effects.isEmpty()) {
                    makeAreaOfEffectCloud(this.referenceStack, effects, result);
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }


    @Override
    protected ItemStack getPickupItem() {
        return switch (getArrowTier()) {
            case FLINT -> new ItemStack(Items.ARROW);
            case IRON -> new ItemStack(ModItems.IRON_UPGRADE_ARROW);
            case STEEL -> new ItemStack(ModItems.STEEL_UPGRADE_ARROW);
            case DIAMOND -> new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW);
        };
    }

    private void multiplyDamage(double factor) {
        setBaseDamage(getBaseDamage() * factor);
    }

    public ArrowTier getArrowTier() {
        int ordinal = this.entityData.get(DATA_TIER);
        return ArrowTier.values()[ordinal % ArrowTier.values().length]; // safety check
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Tier", this.entityData.get(DATA_TIER));
        tag.putInt("PotionColor", this.entityData.get(DATA_POTION_COLOR));
        if (!this.effects.isEmpty()) {
            ListTag listtag = new ListTag();

            for (MobEffectInstance effectInstance : this.effects) {
                listtag.add(effectInstance.save(new CompoundTag()));
            }

            tag.put("CustomPotionEffects", listtag);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("Tier", Tag.TAG_ANY_NUMERIC)) {
            this.entityData.set(DATA_TIER, tag.getByte("Tier"));
        }
        if (tag.contains("PotionColor", Tag.TAG_ANY_NUMERIC)) {
            this.entityData.set(DATA_POTION_COLOR, tag.getInt("PotionColor"));
        }
        this.effects.clear();
        for (MobEffectInstance effectInstance : PotionUtils.getCustomEffects(tag)) {
            this.addEffect(effectInstance);
        }

    }

    public void addEffect(MobEffectInstance effectInstance) {
        this.effects.add(effectInstance);
        CompoundTag tag = this.referenceStack.getTag();
        Potion potion = getPotion(tag);
        this.getEntityData().set(DATA_POTION_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(potion, this.effects)));
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
        cloud.setPotion(potion);

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

    public static List<MobEffectInstance> getMobEffects(ItemStack stack) {
        return getAllEffects(stack.getTag());
    }

    public static Potion getPotion(@Nullable CompoundTag tag) {
        if (tag == null) return Potions.EMPTY;

        // Prioritize "LingeringPotion" if present
        if (tag.contains("LingeringPotion", Tag.TAG_STRING)) {
            return Potion.byName(tag.getString("LingeringPotion"));
        }
        if (tag.contains("LingeringPotion") && tag.getBoolean("LingeringPotion")) {
            return Potion.byName(tag.getString("Potion"));
        }
        if (tag.contains("Potion", Tag.TAG_STRING)) {
            return Potion.byName(tag.getString("Potion"));
        }

        return Potions.EMPTY;
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag compound) {
        List<MobEffectInstance> list = Lists.newArrayList();
        list.addAll(getPotion(compound).getEffects());
        PotionUtils.getCustomEffects(compound, list);
        return list;
    }

    public static void getCustomEffects(@Nullable CompoundTag compound, List<MobEffectInstance> effectList) {
        if (compound != null && compound.contains("CustomPotionEffects", Tag.TAG_LIST)) {
            ListTag listtag = compound.getList("CustomPotionEffects", Tag.TAG_COMPOUND);

            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag nbtCompound = listtag.getCompound(i);
                MobEffectInstance effectInstance = MobEffectInstance.load(nbtCompound);
                if (effectInstance != null) {
                    effectList.add(effectInstance);
                }
            }
        }

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
        if (this.level().isClientSide) {
            if (this.inGround) {
                if (this.inGroundTime % 5 == 0) {
                    this.makeParticle(1);
                }
            } else {
                this.makeParticle(2);
            }

        }

    }
}
