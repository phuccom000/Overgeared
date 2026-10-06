package net.stirdrem.overgeared.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;

/** Common view of the (shaped and shapeless) alloy smelter recipes, used by the block entity and recipe viewers. */
public interface IAlloyRecipe {
    /**
     * Shapeless: every ingredient (all present). Shaped: the pattern, row-major, {@code getWidth() * getHeight()}
     * entries, blanks are {@link Optional#empty()}.
     */
    List<Optional<Ingredient>> getIngredientsList();

    /** A fresh copy of the result. */
    ItemStack getResultItem();

    /** @deprecated use {@link #getResultItem()}. */
    @Deprecated
    default ItemStack getResultItem(HolderLookup.Provider registries) {
        return getResultItem();
    }

    float getExperience();

    int getCookingTime();

    boolean isShaped();

    int getWidth();

    int getHeight();
}
