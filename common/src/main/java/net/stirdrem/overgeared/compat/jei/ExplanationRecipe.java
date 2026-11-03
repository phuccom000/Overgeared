package net.stirdrem.overgeared.compat.jei;

import net.minecraft.world.item.ItemStack;

/**
 * A display-only JEI "recipe" used by the explanation categories
 * (flint knapping, stone anvil, steel anvil). It only carries the result shown.
 */
public record ExplanationRecipe(ItemStack result) {
    public ItemStack getResultItem() {
        return result.copy();
    }
}
