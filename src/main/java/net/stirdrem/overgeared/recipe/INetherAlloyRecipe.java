package net.stirdrem.overgeared.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.DynamicRegistryManager;

import java.util.List;

public interface INetherAlloyRecipe {
    List<Ingredient> getIngredientsList();

    ItemStack getOutput(DynamicRegistryManager registryAccess);

    float getExperience();

    boolean isShaped();

    int getWidth();

    int getHeight();
}
