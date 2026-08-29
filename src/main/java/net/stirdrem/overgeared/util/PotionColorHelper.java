package net.stirdrem.overgeared.util;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Pure NBT-based potion color math shared between common-side entity code (UpgradeArrowEntity)
 * and the client-side item color provider. Kept out of the client package specifically so
 * common code never has to touch a class that could later grow a real client dependency
 * (see FabricModding.md's client/server separation rule).
 */
public class PotionColorHelper {

    public static int getColor(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag != null && tag.contains("CustomPotionColor", NbtElement.NUMBER_TYPE)) {
            return tag.getInt("CustomPotionColor");
        } else {
            return getPotion(tag) == Potions.EMPTY ? 16253176 : getColor(getMobEffects(tag));
        }
    }

    public static int getColor(Collection<StatusEffectInstance> effects) {
        if (effects.isEmpty()) {
            return 3694022;
        } else {
            float r = 0.0F, g = 0.0F, b = 0.0F;
            int total = 0;

            for (StatusEffectInstance effect : effects) {
                if (effect.shouldShowParticles()) {
                    int color = effect.getEffectType().getColor();
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

    public static Potion getPotion(@Nullable NbtCompound tag) {
        if (tag == null) return Potions.EMPTY;

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

    public static List<StatusEffectInstance> getMobEffects(@Nullable NbtCompound tag) {
        return getAllEffects(tag);
    }

    public static List<StatusEffectInstance> getAllEffects(@Nullable NbtCompound tag) {
        List<StatusEffectInstance> list = new java.util.ArrayList<>(getPotion(tag).getEffects());
        getCustomEffects(tag, list);
        return list;
    }

    public static void getCustomEffects(@Nullable NbtCompound tag, List<StatusEffectInstance> effectList) {
        PotionUtil.getCustomPotionEffects(tag, effectList);
    }
}
