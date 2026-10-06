package net.stirdrem.overgeared.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Potion color/effect helpers shared between common-side entity code (UpgradeArrowEntity,
 * LingeringArrowEntity) and the client-side item color provider.
 *
 * <p>26.3 port: potion data is the vanilla {@link DataComponents#POTION_CONTENTS} component now
 * (the old "Potion" / "CustomPotionEffects" / "CustomPotionColor" / "LingeringPotion" NBT keys are gone),
 * so every helper takes the {@link ItemStack} instead of its CompoundTag.
 */
public class PotionColorHelper {

    /** Legacy "no potion" color of the 1.20 PotionUtils. */
    public static final int NO_POTION_COLOR = 16253176;

    public static PotionContents getContents(ItemStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }

    public static int getColor(ItemStack stack) {
        PotionContents contents = getContents(stack);
        if (contents.customColor().isPresent()) {
            return contents.customColor().get();
        }
        if (contents.potion().isEmpty() && contents.customEffects().isEmpty()) {
            return NO_POTION_COLOR;
        }
        return getColor(getAllEffects(stack));
    }

    public static int getColor(Collection<MobEffectInstance> effects) {
        if (effects.isEmpty()) {
            return 3694022;
        } else {
            float r = 0.0F, g = 0.0F, b = 0.0F;
            int total = 0;

            for (MobEffectInstance effect : effects) {
                if (effect.isVisible()) {
                    int color = effect.getEffect().value().getColor();
                    int amplifierWeight = effect.getAmplifier() + 1;
                    r += (float) (amplifierWeight * (color >> 16 & 255)) / 255.0F;
                    g += (float) (amplifierWeight * (color >> 8 & 255)) / 255.0F;
                    b += (float) (amplifierWeight * (color & 255)) / 255.0F;
                    total += amplifierWeight;
                }
            }

            if (total == 0) {
                return 0;
            } else {
                r = r / total * 255.0F;
                g = g / total * 255.0F;
                b = b / total * 255.0F;
                return (int) r << 16 | (int) g << 8 | (int) b;
            }
        }
    }

    /** The base potion of the stack, if any. */
    public static Optional<Holder<Potion>> getPotion(ItemStack stack) {
        return getContents(stack).potion();
    }

    public static List<MobEffectInstance> getMobEffects(ItemStack stack) {
        return getAllEffects(stack);
    }

    /** Base potion effects followed by custom effects (same order as the 1.20 PotionUtils). */
    public static List<MobEffectInstance> getAllEffects(ItemStack stack) {
        List<MobEffectInstance> list = new ArrayList<>();
        getContents(stack).getAllEffects().forEach(list::add);
        return list;
    }

    public static List<MobEffectInstance> getCustomEffects(ItemStack stack) {
        return new ArrayList<>(getContents(stack).customEffects());
    }
}
