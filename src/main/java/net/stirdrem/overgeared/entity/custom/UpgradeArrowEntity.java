package net.stirdrem.overgeared.entity.custom;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.PotionColorHelper;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class UpgradeArrowEntity extends PersistentProjectileEntity {
    private static final TrackedData<Byte> DATA_TIER =
            DataTracker.registerData(UpgradeArrowEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Integer> DATA_POTION_COLOR =
            DataTracker.registerData(UpgradeArrowEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final ItemStack referenceStack;
    private final Set<StatusEffectInstance> effects = Sets.newHashSet();
    private Potion potion = Potions.EMPTY;

    public UpgradeArrowEntity(ArrowTier tier, World world, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.UPGRADE_ARROW, shooter, world);
        this.referenceStack = stack;
        this.dataTracker.set(DATA_TIER, (byte) tier.ordinal());

        // Server-side only: extract potion color
        NbtCompound tag = stack.getNbt();
        int color = -1;
        if (tag != null && (tag.contains("Potion") || tag.contains("CustomPotionEffects") || tag.contains("LingeringPotion")))
            color = PotionColorHelper.getColor(stack);
        this.dataTracker.set(DATA_POTION_COLOR, color);
        this.potion = getPotion(tag);
    }

    public UpgradeArrowEntity(EntityType<? extends PersistentProjectileEntity> type, World world) {
        super(type, world);
        this.referenceStack = ItemStack.EMPTY;
    }

    public UpgradeArrowEntity(ArrowTier tier, World world, double x, double y, double z, ItemStack stack) {
        super(ModEntities.UPGRADE_ARROW, x, y, z, world);
        this.referenceStack = stack;
        this.dataTracker.set(DATA_TIER, (byte) tier.ordinal());

        NbtCompound tag = stack.getNbt();
        int color = -1;
        if (tag != null && (tag.contains("Potion") || tag.contains("CustomPotionEffects") || tag.contains("LingeringPotion"))) {
            color = PotionColorHelper.getColor(stack);
        }
        this.dataTracker.set(DATA_POTION_COLOR, color);
        this.potion = getPotion(tag);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DATA_TIER, (byte) ArrowTier.FLINT.ordinal());
        this.dataTracker.startTracking(DATA_POTION_COLOR, -1); // Default no color
    }

    @Override
    protected void onEntityHit(EntityHitResult result) {
        setDamage(getDamage() * getArrowTier().getDamageBonus());
        super.onEntityHit(result);
    }

    @Override
    protected void onHit(LivingEntity target) {
        super.onHit(target);
        Entity owner = this.getOwner(); // More reliable than getEffectSource()
        if (owner == null) {
            owner = this; // Fallback to the arrow itself
        }
        for (StatusEffectInstance effect : this.potion.getEffects()) {
            if (effect.getEffectType().isInstant()) {
                effect.getEffectType().applyInstantEffect(owner, owner instanceof LivingEntity livingOwner ? livingOwner : null,
                        target, effect.getAmplifier(), 1.0D);
            } else {
                StatusEffectInstance reduced = new StatusEffectInstance(
                        effect.getEffectType(),
                        Math.max(effect.getDuration() / 8, 1),
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.shouldShowParticles(),
                        effect.shouldShowIcon()
                );
                target.addStatusEffect(reduced, owner);
            }
        }

        for (StatusEffectInstance effect : this.effects) {
            if (effect.getEffectType().isInstant()) {
                effect.getEffectType().applyInstantEffect(owner, owner instanceof LivingEntity livingOwner ? livingOwner : null,
                        target, effect.getAmplifier(), 1.0D);
            } else {
                target.addStatusEffect(effect, owner);
            }
        }
    }


    @Override
    protected void onCollision(HitResult result) {
        super.onCollision(result);
        if (!getWorld().isClient) {
            NbtCompound tag = this.referenceStack.getNbt();

            // Only lingering type creates cloud
            if (tag != null && (tag.contains("LingeringPotion") || getArrowTier() == ArrowTier.FLINT && tag.contains("Potion", NbtElement.STRING_TYPE))) {
                Potion potion = getPotion(tag);
                List<StatusEffectInstance> effects = getAllEffects(tag);
                if (!effects.isEmpty()) {
                    makeAreaOfEffectCloud(this.referenceStack, effects, result);
                }
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult result) {
        super.onBlockHit(result);
    }


    @Override
    protected ItemStack asItemStack() {
        return switch (getArrowTier()) {
            case FLINT -> new ItemStack(Items.ARROW);
            case IRON -> new ItemStack(ModItems.IRON_UPGRADE_ARROW);
            case STEEL -> new ItemStack(ModItems.STEEL_UPGRADE_ARROW);
            case DIAMOND -> new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW);
        };
    }

    private void multiplyDamage(double factor) {
        setDamage(getDamage() * factor);
    }

    public ArrowTier getArrowTier() {
        int ordinal = this.dataTracker.get(DATA_TIER);
        return ArrowTier.values()[ordinal % ArrowTier.values().length]; // safety check
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        tag.putByte("Tier", this.dataTracker.get(DATA_TIER));
        tag.putInt("PotionColor", this.dataTracker.get(DATA_POTION_COLOR));
        if (!this.effects.isEmpty()) {
            NbtList listtag = new NbtList();

            for (StatusEffectInstance effectInstance : this.effects) {
                listtag.add(effectInstance.writeNbt(new NbtCompound()));
            }

            tag.put("CustomPotionEffects", listtag);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);

        if (tag.contains("Tier", NbtElement.NUMBER_TYPE)) {
            this.dataTracker.set(DATA_TIER, tag.getByte("Tier"));
        }
        if (tag.contains("PotionColor", NbtElement.NUMBER_TYPE)) {
            this.dataTracker.set(DATA_POTION_COLOR, tag.getInt("PotionColor"));
        }
        this.effects.clear();
        for (StatusEffectInstance effectInstance : PotionUtil.getCustomPotionEffects(tag)) {
            this.addEffect(effectInstance);
        }

    }

    public void addEffect(StatusEffectInstance effectInstance) {
        this.effects.add(effectInstance);
        NbtCompound tag = this.referenceStack.getNbt();
        Potion potion = getPotion(tag);
        this.getDataTracker().set(DATA_POTION_COLOR, PotionUtil.getColor(PotionUtil.getPotionEffects(potion, this.effects)));
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
        cloud.setPotion(potion);

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

    public static List<StatusEffectInstance> getMobEffects(ItemStack stack) {
        return getAllEffects(stack.getNbt());
    }

    public static Potion getPotion(@Nullable NbtCompound tag) {
        if (tag == null) return Potions.EMPTY;

        // Prioritize "LingeringPotion" if present
        if (tag.contains("LingeringPotion", NbtElement.STRING_TYPE)) {
            return Potion.byId(tag.getString("LingeringPotion"));
        }
        if (tag.contains("LingeringPotion") && tag.getBoolean("LingeringPotion")) {
            return Potion.byId(tag.getString("Potion"));
        }
        if (tag.contains("Potion", NbtElement.STRING_TYPE)) {
            return Potion.byId(tag.getString("Potion"));
        }

        return Potions.EMPTY;
    }

    public static List<StatusEffectInstance> getAllEffects(@Nullable NbtCompound compound) {
        List<StatusEffectInstance> list = Lists.newArrayList();
        list.addAll(getPotion(compound).getEffects());
        PotionUtil.getCustomPotionEffects(compound, list);
        return list;
    }

    public static void getCustomEffects(@Nullable NbtCompound compound, List<StatusEffectInstance> effectList) {
        if (compound != null && compound.contains("CustomPotionEffects", NbtElement.LIST_TYPE)) {
            NbtList listtag = compound.getList("CustomPotionEffects", NbtElement.COMPOUND_TYPE);

            for (int i = 0; i < listtag.size(); ++i) {
                NbtCompound nbtCompound = listtag.getCompound(i);
                StatusEffectInstance effectInstance = StatusEffectInstance.fromNbt(nbtCompound);
                if (effectInstance != null) {
                    effectList.add(effectInstance);
                }
            }
        }

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
        if (this.getWorld().isClient) {
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
