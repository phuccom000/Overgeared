package net.stirdrem.overgeared.recipe;

import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public interface IAlloyRecipe {
    List<Ingredient> getIngredientsList();

    ItemStack getResultItem(RegistryAccess registryAccess);

    float getExperience();

    boolean isShaped();

    int getWidth();

    int getHeight();
}
