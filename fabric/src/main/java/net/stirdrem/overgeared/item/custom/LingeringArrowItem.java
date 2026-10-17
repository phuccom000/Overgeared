package net.stirdrem.overgeared.item.custom;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LingeringArrowItem extends ArrowItem {
    private final ArrowTier tier;

    public LingeringArrowItem(Properties settings, ArrowTier tier) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public ItemStack getDefaultInstance() {
        return PotionUtils.setPotion(super.getDefaultInstance(), Potions.POISON);
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter) {
        return new UpgradeArrowEntity(tier, world, shooter, stack);
    }

    public ArrowTier getTier() {
        return tier;
    }

    // NOTE: Forge's ArrowItem#isInfinite(stack, bow, player) extension point (used here to
    // disable the Infinity enchantment for lingering arrows) has no vanilla/Fabric equivalent
    // in 1.20.1 - not ported; Infinity currently behaves as vanilla with this ammo.

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        CompoundTag tag = stack.getTag();
        if (tag != null && (tag.contains("Potion") || tag.contains("CustomPotionEffects"))) {
            PotionUtils.addPotionTooltip(stack, tooltip, 0.125F);
        }
    }

    public static List<MobEffectInstance> getMobEffects(ItemStack stack) {
        return getAllEffects(stack.getTag());
    }

    public static Potion getPotion(@Nullable CompoundTag tag) {
        if (tag == null) return Potions.EMPTY;

        if (tag.contains("Potion", Tag.TAG_STRING)) {
            return Potion.byName(tag.getString("Potion"));
        }

        return Potions.EMPTY;
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag compound) {
        List<MobEffectInstance> list = Lists.newArrayList();
        list.addAll(getPotion(compound).getEffects());
        getCustomEffects(compound, list);
        return list;
    }

    public static void getCustomEffects(@Nullable CompoundTag compound, List<MobEffectInstance> effectList) {
        if (compound != null && compound.contains("CustomPotionEffects", Tag.TAG_LIST)) {
            ListTag list = compound.getList("CustomPotionEffects", Tag.TAG_COMPOUND);

            for (int i = 0; i < list.size(); ++i) {
                CompoundTag nbtCompound = list.getCompound(i);
                MobEffectInstance statusEffectInstance = MobEffectInstance.load(nbtCompound);
                if (statusEffectInstance != null) {
                    effectList.add(statusEffectInstance);
                }
            }
        }

    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && !tag.isEmpty()) {
            // Use getAllEffects to check for both regular potions and custom effects
            List<MobEffectInstance> effects = getAllEffects(tag);
            boolean hasEffects = !effects.isEmpty();

            if (hasEffects) {
                Potion potion = getPotion(tag);

                if (potion != Potions.EMPTY) {
                    String potionId = potion.getName("").replace("effect.minecraft.", "");

                    boolean isNoEffectPotion = potionId.equals("mundane") || potionId.equals("awkward") || potionId.equals("thick");

                    if (!isNoEffectPotion) {
                        String effectKey = "item.overgeared.arrow.effect." + potionId;
                        Component effectComponent = Component.translatable(effectKey);

                        // Determine if it's a Lingering or regular tipped arrow
                        return Component.translatable(getDescriptionId(stack), effectComponent);
                    }
                }
            }
            return Component.translatable(getDescriptionId(stack) + ".no_effect");
        }
        return Component.translatable(getDescriptionId(stack));
    }
}
