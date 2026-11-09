package net.stirdrem.overgeared.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public interface INetherAlloyRecipe extends OvergearedRecipe<RecipeInput> {
    List<Ingredient> getIngredientsList();

    ItemStack getResultItem(HolderLookup.Provider provider);

    float getExperience();

    boolean isShaped();
}
