package net.stirdrem.overgeared.entity.custom;

import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UpgradeArrowEntity extends PotionArrowEntity {
    private static final EntityDataAccessor<Byte> DATA_TIER =
            SynchedEntityData.defineId(UpgradeArrowEntity.class, EntityDataSerializers.BYTE);

    private final Set<MobEffectInstance> effects = Sets.newHashSet();

    public UpgradeArrowEntity(EntityType<? extends UpgradeArrowEntity> type, ArrowTier tier, Level level, LivingEntity shooter, ItemStack stack, @Nullable ItemStack firedFromWeapon) {
        super(type, level, shooter, stack, firedFromWeapon);
        this.entityData.set(DATA_TIER, (byte) tier.ordinal());
    }

    public UpgradeArrowEntity(EntityType<? extends UpgradeArrowEntity> type, Level level) {
        super(type, level);
    }

    public UpgradeArrowEntity(EntityType<? extends UpgradeArrowEntity> type, ArrowTier tier, Level level, double x, double y, double z, ItemStack stack, @Nullable ItemStack firedFromWeapon) {
        super(type, level, x, y, z, stack, firedFromWeapon);
        this.entityData.set(DATA_TIER, (byte) tier.ordinal());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TIER, (byte) ArrowTier.FLINT.ordinal());
    }

    @Override
    protected boolean shouldSpawnCloud() {
        return this.referenceStack.getOrDefault(ModComponents.LINGERING_STATUS.get(), false);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        setBaseDamage(getBaseDamage() * getArrowTier().getDamageBonus());
        super.onHitEntity(result);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        Entity owner = this.getOwner();
        if (owner == null) {
            owner = this;
        }

        for (MobEffectInstance effect : this.potionContents.getAllEffects()) {
            applyEffect(target, owner, effect, true);
        }
        for (MobEffectInstance effect : this.effects) {
            applyEffect(target, owner, effect, false);
        }
    }

    private static void applyEffect(LivingEntity target, Entity owner, MobEffectInstance effect, boolean reduce) {
        if (effect.getEffect().value().isInstantenous()) {
            effect.getEffect().value().applyInstantenousEffect(owner,
                    owner instanceof LivingEntity livingOwner ? livingOwner : null,
                    target, effect.getAmplifier(), 1.0D);
            return;
        }
        target.addEffect(reduce ? reducedDuration(effect) : effect, owner);
    }

    @Override
    protected ItemStack getPickupItem() {
        return switch (getArrowTier()) {
            case FLINT -> new ItemStack(Items.ARROW);
            case IRON -> new ItemStack(ModItems.IRON_UPGRADE_ARROW.get());
            case STEEL -> new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get());
            case DIAMOND -> new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW.get());
        };
    }

    public ArrowTier getArrowTier() {
        int ordinal = this.entityData.get(DATA_TIER);
        return ArrowTier.values()[ordinal % ArrowTier.values().length];
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Tier", this.entityData.get(DATA_TIER));

        if (!this.effects.isEmpty()) {
            ListTag list = new ListTag();
            for (MobEffectInstance effect : this.effects) {
                list.add(effect.save());
            }
            tag.put("CustomPotionEffects", list);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Tier", 99)) {
            this.entityData.set(DATA_TIER, tag.getByte("Tier"));
        }

        this.effects.clear();
        if (tag.contains("CustomPotionEffects", 9)) {
            ListTag list = tag.getList("CustomPotionEffects", 10);
            for (int i = 0; i < list.size(); ++i) {
                MobEffectInstance effect = MobEffectInstance.load(list.getCompound(i));
                if (effect != null) {
                    this.addEffect(effect);
                }
            }
        }
    }

    public void addEffect(MobEffectInstance effect) {
        this.effects.add(effect);

        List<MobEffectInstance> all = new ArrayList<>();
        for (MobEffectInstance e : this.potionContents.getAllEffects()) {
            all.add(e);
        }
        all.addAll(this.effects);
        PotionContents combined = new PotionContents(this.potionContents.potion(), this.potionContents.customColor(), all);
        this.entityData.set(DATA_POTION_COLOR, combined.getColor());
    }
}
