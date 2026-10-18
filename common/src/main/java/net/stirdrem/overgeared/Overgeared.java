package net.stirdrem.overgeared;

import com.google.common.collect.Lists;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.stirdrem.overgeared.config.ServerConfigCommon;
import net.stirdrem.overgeared.platform.Services;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class Overgeared {
    public static final String MOD_ID = Constants.MOD_ID;
    public static final String MOD_NAME = Constants.MOD_NAME;
    public static final Logger LOGGER = Constants.LOG;

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Overgeared.MOD_ID, path);
    }

    public static void init() {

        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
        Constants.LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));
        if (Services.PLATFORM.isModLoaded(Constants.MOD_ID)) {

            Constants.LOG.info("Hello to Overgeared");
        }
    }

    public static ServerConfigCommon getConfig() {
        return Services.PLATFORM.getConfig();
    }

    public static int getColor(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTag();
        if (compoundtag != null && compoundtag.contains("CustomPotionColor", 99)) {
            return compoundtag.getInt("CustomPotionColor");
        } else {
            return getPotion(pStack) == Potions.EMPTY ? 16253176 : getColor(getMobEffects(pStack));
        }
    }

    public static int getColor(Collection<MobEffectInstance> pEffects) {
        int i = 3694022;
        if (pEffects.isEmpty()) {
            return 3694022;
        } else {
            float f = 0.0F;
            float f1 = 0.0F;
            float f2 = 0.0F;
            int j = 0;

            for (MobEffectInstance mobeffectinstance : pEffects) {
                if (mobeffectinstance.isVisible()) {
                    int k = mobeffectinstance.getEffect().getColor();
                    int l = mobeffectinstance.getAmplifier() + 1;
                    f += (float) (l * (k >> 16 & 255)) / 255.0F;
                    f1 += (float) (l * (k >> 8 & 255)) / 255.0F;
                    f2 += (float) (l * (k >> 0 & 255)) / 255.0F;
                    j += l;
                }
            }

            if (j == 0) {
                return 0;
            } else {
                f = f / (float) j * 255.0F;
                f1 = f1 / (float) j * 255.0F;
                f2 = f2 / (float) j * 255.0F;
                return (int) f << 16 | (int) f1 << 8 | (int) f2;
            }
        }
    }

    public static Potion getPotion(ItemStack pStack) {
        return getPotion(pStack.getTag());
    }

    public static Potion getPotion(@Nullable CompoundTag tag) {
        if (tag == null) return Potions.EMPTY;

        // Prioritize "LingeringPotion" if present
        if (tag.contains("LingeringPotion", 8)) { // 8 = string type
            return Potion.byName(tag.getString("LingeringPotion"));
        }
        if (tag.contains("LingeringPotion") && tag.getBoolean("LingeringPotion")) { // 8 = string type
            return Potion.byName(tag.getString("Potion"));
        }
        if (tag.contains("Potion", 8)) {
            return Potion.byName(tag.getString("Potion"));
        }

        return Potions.EMPTY;
    }


    public static List<MobEffectInstance> getMobEffects(ItemStack pStack) {
        return getAllEffects(pStack.getTag());
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag pCompoundTag) {
        List<MobEffectInstance> list = Lists.newArrayList();
        list.addAll(getPotion(pCompoundTag).getEffects());
        getCustomEffects(pCompoundTag, list);
        return list;
    }

    public static void getCustomEffects(@Nullable CompoundTag pCompoundTag, List<MobEffectInstance> pEffectList) {
        if (pCompoundTag != null && pCompoundTag.contains("CustomPotionEffects", 9)) {
            ListTag listtag = pCompoundTag.getList("CustomPotionEffects", 10);

            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                MobEffectInstance mobeffectinstance = MobEffectInstance.load(compoundtag);
                if (mobeffectinstance != null) {
                    pEffectList.add(mobeffectinstance);
                }
            }
        }

    }
}