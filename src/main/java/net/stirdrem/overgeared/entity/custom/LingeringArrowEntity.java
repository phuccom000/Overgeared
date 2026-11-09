package net.stirdrem.overgeared.entity.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.entity.ModEntities;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Vanilla-style tipped arrow that additionally drops a lingering cloud on impact. The potion
 * (and its synced color/particles) is handled by vanilla {@link Arrow} from the pickup stack's
 * {@link DataComponents#POTION_CONTENTS}.
 */
public class LingeringArrowEntity extends Arrow {

    public LingeringArrowEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.LINGERING_ARROW, level);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1F, shooter.getZ());
        this.setOwner(shooter);
        this.setPickupItemStack(stack.copy());
    }

    public LingeringArrowEntity(EntityType<? extends Arrow> type, Level level) {
        super(type, level);
    }

    private PotionContents getContents() {
        return getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide()) {
            PotionContents contents = getContents();
            if (contents.getAllEffects().iterator().hasNext()) {
                makeAreaOfEffectCloud(contents, result);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide()) {
            PotionContents contents = getContents();
            if (contents.getAllEffects().iterator().hasNext()) {
                makeAreaOfEffectCloud(contents, result);
            }
        }
    }

    private void makeAreaOfEffectCloud(PotionContents contents, HitResult result) {
        AreaEffectCloud cloud = getAreaEffectCloudEntity(result);
        Entity owner = getOwner();
        if (owner instanceof LivingEntity le) {
            cloud.setOwner(le);
        }

        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());

        List<MobEffectInstance> reduced = new ArrayList<>();
        for (MobEffectInstance inst : contents.getAllEffects()) {
            reduced.add(new MobEffectInstance(
                    inst.getEffect(),
                    Math.max(inst.getDuration() / 8, 1),
                    inst.getAmplifier(),
                    inst.isAmbient(),
                    inst.isVisible(),
                    inst.showIcon()
            ));
        }
        // customColor carries over the old CustomPotionColor -> setFixedColor behaviour.
        cloud.setPotionContents(new PotionContents(contents.potion(), contents.customColor(), reduced, contents.customName()));

        level().addFreshEntity(cloud);
    }

    private @NotNull AreaEffectCloud getAreaEffectCloudEntity(HitResult result) {
        Vec3 hit = result.getLocation();

        // Compute vertical motion ratio
        Vec3 motion = this.getDeltaMovement();
        double verticalRatio = motion.y / motion.length(); // -1 to 1

        // Map verticalRatio to offset: more vertical -> larger downward offset
        double offset = verticalRatio > 0 ? -verticalRatio * 0.5 : -0.2;

        return new AreaEffectCloud(level(), hit.x, hit.y + offset + 0.25, hit.z);
    }
}
