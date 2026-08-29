package net.stirdrem.overgeared.item;

import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

import java.util.function.Supplier;

public class ModToolTiers {
    // Forge's ForgeTier/TierSortingRegistry has no Fabric equivalent; ordering between
    // vanilla tiers only matters for the mineable/needs_*_tool tags (see ModTags.Blocks),
    // which already encode the same steel≈iron, copper≈stone placement.
    public static final ToolMaterial STEEL = new SimpleToolMaterial(
            3, 500, 7.0F, 3.0F, 12, () -> Ingredient.ofItems(ModItems.STEEL_INGOT));

    public static final ToolMaterial COPPER = new SimpleToolMaterial(
            2, 190, 5.0F, 1.0F, 12, () -> Ingredient.ofItems(Items.COPPER_INGOT));

    private record SimpleToolMaterial(int miningLevel, int durability, float miningSpeed, float attackDamage,
                                      int enchantability,
                                      Supplier<Ingredient> repairIngredient) implements ToolMaterial {
        @Override
        public int getDurability() {
            return durability;
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return miningSpeed;
        }

        @Override
        public float getAttackDamage() {
            return attackDamage;
        }

        @Override
        public int getMiningLevel() {
            return miningLevel;
        }

        @Override
        public int getEnchantability() {
            return enchantability;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return repairIngredient.get();
        }
    }
}
