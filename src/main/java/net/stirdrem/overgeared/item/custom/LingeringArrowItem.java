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
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LingeringArrowItem extends ArrowItem {
    private final ArrowTier tier;

    public LingeringArrowItem(Settings settings, ArrowTier tier) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public ItemStack getDefaultStack() {
        return PotionUtil.setPotion(super.getDefaultStack(), Potions.POISON);
    }

    @Override
    public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
        return new UpgradeArrowEntity(tier, world, shooter, stack);
    }

    public ArrowTier getTier() {
        return tier;
    }

    // NOTE: Forge's ArrowItem#isInfinite(stack, bow, player) extension point (used here to
    // disable the Infinity enchantment for lingering arrows) has no vanilla/Fabric equivalent
    // in 1.20.1 - not ported; Infinity currently behaves as vanilla with this ammo.

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound tag = stack.getNbt();
        if (tag != null && (tag.contains("Potion") || tag.contains("CustomPotionEffects"))) {
            PotionUtil.buildTooltip(stack, tooltip, 0.125F);
        }
    }

    public static List<StatusEffectInstance> getMobEffects(ItemStack stack) {
        return getAllEffects(stack.getNbt());
    }

    public static Potion getPotion(@Nullable NbtCompound tag) {
        if (tag == null) return Potions.EMPTY;

        if (tag.contains("Potion", NbtElement.STRING_TYPE)) {
            return Potion.byId(tag.getString("Potion"));
        }

        return Potions.EMPTY;
    }

    public static List<StatusEffectInstance> getAllEffects(@Nullable NbtCompound compound) {
        List<StatusEffectInstance> list = Lists.newArrayList();
        list.addAll(getPotion(compound).getEffects());
        getCustomEffects(compound, list);
        return list;
    }

    public static void getCustomEffects(@Nullable NbtCompound compound, List<StatusEffectInstance> effectList) {
        if (compound != null && compound.contains("CustomPotionEffects", NbtElement.LIST_TYPE)) {
            NbtList list = compound.getList("CustomPotionEffects", NbtElement.COMPOUND_TYPE);

            for (int i = 0; i < list.size(); ++i) {
                NbtCompound nbtCompound = list.getCompound(i);
                StatusEffectInstance statusEffectInstance = StatusEffectInstance.fromNbt(nbtCompound);
                if (statusEffectInstance != null) {
                    effectList.add(statusEffectInstance);
                }
            }
        }

    }

    @Override
    public Text getName(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag != null && !tag.isEmpty()) {
            // Use getAllEffects to check for both regular potions and custom effects
            List<StatusEffectInstance> effects = getAllEffects(tag);
            boolean hasEffects = !effects.isEmpty();

            if (hasEffects) {
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
            }
            return Text.translatable(getTranslationKey(stack) + ".no_effect");
        }
        return Text.translatable(getTranslationKey(stack));
    }
}
