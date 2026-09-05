package net.stirdrem.overgeared.util;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

/**
 * Pure NBT-based potion color math shared between common-side entity code (UpgradeArrowEntity)
 * and the client-side item color provider. Kept out of the client package specifically so
 * common code never has to touch a class that could later grow a real client dependency
 * (see FabricModding.md's client/server separation rule).
 */
public class PotionColorHelper {

    public static int getColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CustomPotionColor", Tag.TAG_ANY_NUMERIC)) {
            return tag.getInt("CustomPotionColor");
        } else {
            return getPotion(tag) == Potions.EMPTY ? 16253176 : getColor(getMobEffects(tag));
        }
    }

    public static int getColor(Collection<MobEffectInstance> effects) {
        if (effects.isEmpty()) {
            return 3694022;
        } else {
            float r = 0.0F, g = 0.0F, b = 0.0F;
            int total = 0;

            for (MobEffectInstance effect : effects) {
                if (effect.isVisible()) {
                    int color = effect.getEffect().getColor();
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

    public static Potion getPotion(@Nullable CompoundTag tag) {
        if (tag == null) return Potions.EMPTY;

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

    public static List<MobEffectInstance> getMobEffects(@Nullable CompoundTag tag) {
        return getAllEffects(tag);
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag tag) {
        List<MobEffectInstance> list = new java.util.ArrayList<>(getPotion(tag).getEffects());
        getCustomEffects(tag, list);
        return list;
    }

    public static void getCustomEffects(@Nullable CompoundTag tag, List<MobEffectInstance> effectList) {
        PotionUtils.getCustomEffects(tag, effectList);
    }
}
