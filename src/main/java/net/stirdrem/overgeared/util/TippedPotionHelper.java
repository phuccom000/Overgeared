package net.stirdrem.overgeared.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.stirdrem.overgeared.components.ModComponents;

/**
 * A potion that has been used to tip arrows by hand gets weaker the more it has been used.
 *
 * <p>26.3 port: the 1.20 PotionItemMixin (rewriting PotionItem#finishUsingItem and the potion
 * tooltip) is replaced by vanilla's {@link DataComponents#POTION_DURATION_SCALE}, which both
 * consumption (PotionContents#onConsume) and the tooltip already honour. Always set the uses
 * through {@link #setTippedUses} so both components stay in sync.
 */
public final class TippedPotionHelper {
    public static final float MIN_DURATION_SCALE = 0.1f;

    private TippedPotionHelper() {
    }

    public static float calculateDurationScale(int tippedUsed) {
        return Math.max(MIN_DURATION_SCALE, 1.0f - (tippedUsed / 8.0f));
    }

    public static int getTippedUses(ItemStack stack) {
        return stack.getOrDefault(ModComponents.TIPPED_USES, 0);
    }

    /**
     * Carries TIPPED_USES over to a brewed potion. As in 1.20.1 the weakening only applies to
     * drinkable potions: a splash or lingering potion keeps the count but its normal duration.
     */
    public static void restoreAfterBrewing(ItemStack brewed, int uses) {
        if (brewed.is(Items.POTION)) {
            setTippedUses(brewed, uses);
            return;
        }
        brewed.set(ModComponents.TIPPED_USES, uses);
        Float defaultScale = brewed.getItem().components().get(DataComponents.POTION_DURATION_SCALE);
        if (defaultScale != null) {
            brewed.set(DataComponents.POTION_DURATION_SCALE, defaultScale);
        } else if (brewed.has(DataComponents.POTION_DURATION_SCALE)) {
            brewed.remove(DataComponents.POTION_DURATION_SCALE);
        }
    }

    /** Sets TIPPED_USES and scales the item's default potion duration scale accordingly. */
    public static void setTippedUses(ItemStack stack, int uses) {
        stack.set(ModComponents.TIPPED_USES, uses);
        float base = stack.getItem().components().getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0f);
        stack.set(DataComponents.POTION_DURATION_SCALE, base * calculateDurationScale(uses));
    }
}
