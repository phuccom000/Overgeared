package net.stirdrem.overgeared.entity.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Iron/steel/diamond (and flint lingering) arrow. Potion data lives on the pickup item stack
 * (vanilla {@link DataComponents#POTION_CONTENTS} + {@link ModComponents#LINGERING_STATUS}),
 * which AbstractArrow already persists, so the potion survives chunk reloads.
 */
public class UpgradeArrowEntity extends AbstractArrow {
    private static final EntityDataAccessor<Byte> DATA_TIER =
            SynchedEntityData.defineId(UpgradeArrowEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_POTION_COLOR =
            SynchedEntityData.defineId(UpgradeArrowEntity.class, EntityDataSerializers.INT);

    private final Set<MobEffectInstance> effects = new LinkedHashSet<>();
    // AbstractArrow has no getter for baseDamage in 26.3; mirror it so the tier bonus can scale it.
    private double trackedBaseDamage = 2.0;

    public UpgradeArrowEntity(ArrowTier tier, Level level, LivingEntity shooter, ItemStack pickup, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.UPGRADE_ARROW, shooter, level, pickup, firedFromWeapon);
        initFromStack(tier);
    }

    public UpgradeArrowEntity(ArrowTier tier, Level level, double x, double y, double z, ItemStack pickup, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.UPGRADE_ARROW, x, y, z, level, pickup, firedFromWeapon);
        initFromStack(tier);
    }

    /** Dispenser-style constructor (no weapon). */
    public UpgradeArrowEntity(ArrowTier tier, Level level, double x, double y, double z, ItemStack pickup) {
        this(tier, level, x, y, z, pickup, null);
    }

    /** EntityType factory constructor. */
    public UpgradeArrowEntity(EntityType<? extends UpgradeArrowEntity> type, Level level) {
        super(type, level);
    }

    private void initFromStack(ArrowTier tier) {
        this.entityData.set(DATA_TIER, (byte) tier.ordinal());
        updateColor();
    }

    private PotionContents getPotionContents() {
        return getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }

    private void updateColor() {
        PotionContents contents = getPotionContents();
        if (!this.effects.isEmpty()) {
            List<MobEffectInstance> all = new ArrayList<>(contents.customEffects());
            all.addAll(this.effects);
            contents = new PotionContents(contents.potion(), contents.customColor(), all, contents.customName());
        }
        this.entityData.set(DATA_POTION_COLOR, contents.equals(PotionContents.EMPTY) ? -1 : contents.getColor());
    }

    @Override
    protected void setPickupItemStack(ItemStack itemStack) {
        super.setPickupItemStack(itemStack);
        updateColor();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TIER, (byte) ArrowTier.FLINT.ordinal());
        builder.define(DATA_POTION_COLOR, -1); // Default no color
    }

    @Override
    public void setBaseDamage(double baseDamage) {
        super.setBaseDamage(baseDamage);
        this.trackedBaseDamage = baseDamage;
    }

    public double getTrackedBaseDamage() {
        return trackedBaseDamage;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        setBaseDamage(trackedBaseDamage * getArrowTier().getDamageBonus());
        super.onHitEntity(result);
        createLingeringCloud(result);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        createLingeringCloud(result);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        if (!(level() instanceof ServerLevel serverLevel)) return;

        Entity owner = this.getOwner(); // More reliable than getEffectSource()
        if (owner == null) {
            owner = this; // Fallback to the arrow itself
        }
        for (MobEffectInstance effect : getPotionContents().getAllEffects()) {
            if (effect.getEffect().value().isInstantaneous()) {
                effect.getEffect().value().applyInstantaneousEffect(serverLevel, owner, owner, target,
                        effect.getAmplifier(), 1.0D);
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
            if (effect.getEffect().value().isInstantaneous()) {
                effect.getEffect().value().applyInstantaneousEffect(serverLevel, owner, owner, target,
                        effect.getAmplifier(), 1.0D);
            } else {
                target.addEffect(new MobEffectInstance(effect), owner);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return getDefaultPickupItem();
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return switch (getArrowTier()) {
            case FLINT -> new ItemStack(Items.ARROW);
            case IRON -> new ItemStack(ModItems.IRON_UPGRADE_ARROW);
            case STEEL -> new ItemStack(ModItems.STEEL_UPGRADE_ARROW);
            case DIAMOND -> new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW);
        };
    }

    public ArrowTier getArrowTier() {
        int ordinal = this.entityData.get(DATA_TIER);
        return ArrowTier.values()[ordinal % ArrowTier.values().length]; // safety check
    }

    /** Synced potion color (ARGB) or -1 when the arrow carries no potion. */
    public int getPotionColor() {
        return this.entityData.get(DATA_POTION_COLOR);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("Tier", this.entityData.get(DATA_TIER));
        output.putInt("PotionColor", this.entityData.get(DATA_POTION_COLOR));
        if (!this.effects.isEmpty()) {
            output.store("CustomPotionEffects", MobEffectInstance.CODEC.listOf(), List.copyOf(this.effects));
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.trackedBaseDamage = input.getDoubleOr("damage", 2.0);
        this.entityData.set(DATA_TIER, input.getByteOr("Tier", (byte) ArrowTier.FLINT.ordinal()));
        this.effects.clear();
        input.read("CustomPotionEffects", MobEffectInstance.CODEC.listOf()).ifPresent(this.effects::addAll);
        updateColor();
        input.getInt("PotionColor").ifPresent(color -> this.entityData.set(DATA_POTION_COLOR, color));
    }

    public void addEffect(MobEffectInstance effectInstance) {
        this.effects.add(effectInstance);
        updateColor();
    }

    private boolean isLingering() {
        ItemStack stack = getPickupItemStackOrigin();
        if (Boolean.TRUE.equals(stack.get(ModComponents.LINGERING_STATUS))) return true;
        // Flint-tier arrows only exist as the lingering arrow item: any potion on them lingers.
        return getArrowTier() == ArrowTier.FLINT && getPotionContents().potion().isPresent();
    }

    private void createLingeringCloud(HitResult result) {
        if (level().isClientSide()) {
            return;
        }

        if (isLingering()) {
            PotionContents contents = getPotionContents();
            if (contents.getAllEffects().iterator().hasNext()) {
                makeAreaOfEffectCloud(contents, result);
            }
        }
    }

    private void makeAreaOfEffectCloud(PotionContents contents, HitResult result) {
        Vec3 hit = result.getLocation();

        // Compute vertical motion ratio
        Vec3 motion = this.getDeltaMovement();
        double verticalRatio = motion.y / motion.length(); // -1 to 1

        // Map verticalRatio to offset: more vertical -> larger downward offset
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

        // Reduced-duration copies of every effect (1/8 duration), on top of the base potion,
        // matching the old setPotion(potion) + addEffect(reduced...) behaviour.
        List<MobEffectInstance> reducedEffects = new ArrayList<>();
        for (MobEffectInstance inst : contents.getAllEffects()) {
            reducedEffects.add(new MobEffectInstance(
                    inst.getEffect(),
                    Math.max(inst.getDuration() / 8, 1),
                    inst.getAmplifier(),
                    inst.isAmbient(),
                    inst.isVisible(),
                    inst.showIcon()
            ));
        }
        cloud.setPotionContents(new PotionContents(contents.potion(), contents.customColor(), reducedEffects, contents.customName()));

        level().addFreshEntity(cloud);
    }

    private void makeParticle(int amount) {
        int color = getPotionColor();
        if (color != -1 && amount > 0) {
            for (int j = 0; j < amount; ++j) {
                this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color),
                        this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
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
