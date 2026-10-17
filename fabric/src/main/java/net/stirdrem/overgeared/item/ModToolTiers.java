package net.stirdrem.overgeared.item;

import java.util.function.Supplier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class ModToolTiers {
    // Forge's ForgeTier/TierSortingRegistry has no Fabric equivalent; ordering between
    // vanilla tiers only matters for the mineable/needs_*_tool tags (see ModTags.Blocks),
    // which already encode the same steel≈iron, copper≈stone placement.
    public static final Tier STEEL = new SimpleToolMaterial(
            2, 500, 7.0F, 3.0F, 12, () -> Ingredient.of(ModItems.STEEL_INGOT));

    public static final Tier COPPER = new SimpleToolMaterial(
            1, 190, 5.0F, 1.0F, 12, () -> Ingredient.of(Items.COPPER_INGOT));

    private record SimpleToolMaterial(int miningLevel, int durability, float miningSpeed, float attackDamage,
                                       int enchantability,
                                       Supplier<Ingredient> repairIngredient) implements Tier {
        @Override
        public int getUses() {
            return durability;
        }

        @Override
        public float getSpeed() {
            return miningSpeed;
        }

        @Override
        public float getAttackDamageBonus() {
            return attackDamage;
        }

        @Override
        public int getLevel() {
            return miningLevel;
        }

        @Override
        public int getEnchantmentValue() {
            return enchantability;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return repairIngredient.get();
        }
    }
}
