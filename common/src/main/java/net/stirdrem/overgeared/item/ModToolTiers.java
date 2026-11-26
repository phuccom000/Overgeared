package net.stirdrem.overgeared.item;

import com.google.common.base.Suppliers;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.stirdrem.overgeared.util.ModTags;

import java.util.function.Supplier;

public enum ModToolTiers implements Tier {

    COPPER(
            190,
            5.0f,
            1.0f,
            12,
            ModTags.Blocks.INCORRECT_FOR_COPPER_TOOL,
            () -> Ingredient.of(Items.COPPER_INGOT)
    ),
    STEEL(
            500,
            7.0f,
            3.0f,
            12,
            ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL,
            () -> Ingredient.of(ModItems.STEEL_INGOT.get())
    );

    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final TagKey<Block> incorrectBlocks;
    // Resolved lazily: other mods may reference these tiers during item registration,
    // before ModItems.STEEL_INGOT is bound.
    private final Supplier<Ingredient> repairIngredient;

    ModToolTiers(
            int uses,
            float speed,
            float damage,
            int enchantmentValue,
            TagKey<Block> incorrectBlocks,
            Supplier<Ingredient> repairIngredient
    ) {
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.incorrectBlocks = incorrectBlocks;
        this.repairIngredient = Suppliers.memoize(repairIngredient::get);
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return damage;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectBlocks;
    }
}