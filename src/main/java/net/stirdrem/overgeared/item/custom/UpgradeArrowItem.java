package net.stirdrem.overgeared.item.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Iron/steel/diamond arrows. Potion effects live in the vanilla POTION_CONTENTS component; the
 * potion tooltip comes from that component (scaled by POTION_DURATION_SCALE, set in ModItems).
 */
public class UpgradeArrowItem extends ArrowItem {
    private final ArrowTier tier;

    public UpgradeArrowItem(Properties settings, ArrowTier tier) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter, @Nullable ItemStack firedFromWeapon) {
        return new UpgradeArrowEntity(tier, world, shooter, stack, firedFromWeapon);
    }

    public ArrowTier getTier() {
        return tier;
    }

    private String getNameKey(ItemStack stack) {
        String tierName = switch (this.tier) {
            case IRON -> "item.overgeared.iron_arrow";
            case STEEL -> "item.overgeared.steel_arrow";
            case DIAMOND -> "item.overgeared.diamond_arrow";
            default -> "item.overgeared.arrow";
        };
        if (Boolean.TRUE.equals(stack.get(ModComponents.LINGERING_STATUS))) {
            return tierName + ".lingering_named";
        }
        if (stack.has(DataComponents.POTION_CONTENTS)) {
            return tierName + ".tipped_named";
        }
        return getDescriptionId();
    }

    @Override
    public Component getName(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return super.getName(stack);
        }

        String nameKey = getNameKey(stack);
        if (contents.potion().isPresent()) {
            String potionId = contents.potion().get().value().name();
            boolean isNoEffectPotion = potionId.equals("mundane") || potionId.equals("awkward") || potionId.equals("thick");
            if (!isNoEffectPotion) {
                return Component.translatable(nameKey, Component.translatable("item.overgeared.arrow.effect." + potionId));
            }
        }
        return Component.translatable(nameKey + ".no_effect");
    }
}
