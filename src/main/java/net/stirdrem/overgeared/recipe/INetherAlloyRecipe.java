package net.stirdrem.overgeared.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;

public interface INetherAlloyRecipe {
    List<Ingredient> getIngredientsList();

    ItemStack getResultItem(RegistryAccess registryAccess);

    float getExperience();

    boolean isShaped();

    int getWidth();

    int getHeight();
}
