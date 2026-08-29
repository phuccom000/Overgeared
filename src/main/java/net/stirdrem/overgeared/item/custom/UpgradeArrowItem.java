package net.stirdrem.overgeared.item.custom;

import com.google.common.collect.Lists;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UpgradeArrowItem extends ArrowItem {
    private final ArrowTier tier;

    public UpgradeArrowItem(Settings settings, ArrowTier tier) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
        return new UpgradeArrowEntity(tier, world, shooter, stack);
    }

    public ArrowTier getTier() {
        return tier;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound tag = stack.getNbt();
        if (tag != null && (tag.contains("Potion") || tag.contains("CustomPotionEffects"))) {
            PotionUtil.buildTooltip(stack, tooltip, 0.125F);
        }
        if (tag != null && (tag.contains("LingeringPotion", NbtElement.STRING_TYPE))) {
            PotionUtil.buildTooltip(getMobEffects(stack), tooltip, 0.125F);
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag != null) {
            String tierName = switch (this.tier) {
                case IRON -> "item.overgeared.iron_arrow";
                case STEEL -> "item.overgeared.steel_arrow";
                case DIAMOND -> "item.overgeared.diamond_arrow";
                default -> "item.overgeared.arrow";
            };

            if (tag.contains("LingeringPotion", NbtElement.BYTE_TYPE) && tag.getBoolean("LingeringPotion")) {
                return tierName + ".lingering_named";
            } else if (tag.contains("LingeringPotion", NbtElement.STRING_TYPE)) {
                return tierName + ".lingering_named";
            } else if (tag.contains("Potion", NbtElement.STRING_TYPE) || tag.contains("CustomPotionEffects", NbtElement.LIST_TYPE)) {
                return tierName + ".tipped_named";
            }
        }

        return super.getTranslationKey(stack);
    }


    public static List<StatusEffectInstance> getMobEffects(ItemStack stack) {
        return getAllEffects(stack.getNbt());
    }

    public static Potion getPotion(@Nullable NbtCompound tag) {
        if (tag == null) return Potions.EMPTY;

        // Prioritize "LingeringPotion" if present
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

    public static List<StatusEffectInstance> getAllEffects(@Nullable NbtCompound compound) {
        List<StatusEffectInstance> list = Lists.newArrayList();
        list.addAll(getPotion(compound).getEffects());
        PotionUtil.getCustomPotionEffects(compound, list);
        return list;
    }

    @Override
    public Text getName(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag != null && !tag.isEmpty()) {
            Potion potion = getPotion(tag);
            if (potion != Potions.EMPTY) {
                String potionId = potion.finishTranslationKey("").replace("effect.minecraft.", "");

                boolean isNoEffectPotion = potionId.equals("mundane") || potionId.equals("awkward") || potionId.equals("thick");

                if (!isNoEffectPotion) {
                    String effectKey = "item.overgeared.arrow.effect." + potionId;
                    Text effectComponent = Text.translatable(effectKey);

                    // Determine if it's a Lingering or regular tipped arrow
                    return Text.translatable(getTranslationKey(stack), effectComponent);
                }
            }
            return Text.translatable(getTranslationKey(stack) + ".no_effect");
        }
        return Text.translatable(getTranslationKey(stack));
    }
}
