package net.stirdrem.overgeared.item.custom;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
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

public class UpgradeArrowItem extends ArrowItem {
    private final ArrowTier tier;

    public UpgradeArrowItem(Properties settings, ArrowTier tier) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter) {
        return new UpgradeArrowEntity(tier, world, shooter, stack);
    }

    public ArrowTier getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        CompoundTag tag = stack.getTag();
        if (tag != null && (tag.contains("Potion") || tag.contains("CustomPotionEffects"))) {
            PotionUtils.addPotionTooltip(stack, tooltip, 0.125F);
        }
        if (tag != null && (tag.contains("LingeringPotion", Tag.TAG_STRING))) {
            PotionUtils.addPotionTooltip(getMobEffects(stack), tooltip, 0.125F);
        }
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            String tierName = switch (this.tier) {
                case IRON -> "item.overgeared.iron_arrow";
                case STEEL -> "item.overgeared.steel_arrow";
                case DIAMOND -> "item.overgeared.diamond_arrow";
                default -> "item.overgeared.arrow";
            };

            if (tag.contains("LingeringPotion", Tag.TAG_BYTE) && tag.getBoolean("LingeringPotion")) {
                return tierName + ".lingering_named";
            } else if (tag.contains("LingeringPotion", Tag.TAG_STRING)) {
                return tierName + ".lingering_named";
            } else if (tag.contains("Potion", Tag.TAG_STRING) || tag.contains("CustomPotionEffects", Tag.TAG_LIST)) {
                return tierName + ".tipped_named";
            }
        }

        return super.getDescriptionId(stack);
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

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && !tag.isEmpty()) {
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
            return Component.translatable(getDescriptionId(stack) + ".no_effect");
        }
        return Component.translatable(getDescriptionId(stack));
    }
}
