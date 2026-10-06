package net.stirdrem.overgeared.item.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Flint lingering arrow. Potion effects live in the vanilla POTION_CONTENTS component; the
 * potion tooltip comes from that component (scaled by POTION_DURATION_SCALE, set in ModItems).
 */
public class LingeringArrowItem extends ArrowItem {
    private final ArrowTier tier;

    public LingeringArrowItem(Properties settings, ArrowTier tier) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.POISON));
        return stack;
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter, @Nullable ItemStack firedFromWeapon) {
        return new UpgradeArrowEntity(tier, world, shooter, stack, firedFromWeapon);
    }

    public ArrowTier getTier() {
        return tier;
    }

    // NOTE: Forge's ArrowItem#isInfinite(stack, bow, player) extension point (used to disable the
    // Infinity enchantment for lingering arrows) has no vanilla/Fabric equivalent - Infinity
    // behaves as vanilla with this ammo.

    @Override
    public Component getName(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return super.getName(stack);
        }
        if (contents.hasEffects() && contents.potion().isPresent()) {
            String potionId = contents.potion().get().value().name();
            boolean isNoEffectPotion = potionId.equals("mundane") || potionId.equals("awkward") || potionId.equals("thick");
            if (!isNoEffectPotion) {
                return Component.translatable(getDescriptionId(), Component.translatable("item.overgeared.arrow.effect." + potionId));
            }
        }
        return Component.translatable(getDescriptionId() + ".no_effect");
    }
}
