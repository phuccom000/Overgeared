package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.recipe.AlloySmeltingRecipe;
import net.stirdrem.overgeared.recipe.ShapedAlloySmeltingRecipe;

/**
 * EMI recipe display for Alloy Smelting recipes (2x2 input grid).
 */
public class ShapedAlloySmeltingEmiRecipe extends AbstractAlloySmeltingEmiRecipe<ShapedAlloySmeltingRecipe> {

    public ShapedAlloySmeltingEmiRecipe(RecipeHolder<ShapedAlloySmeltingRecipe> holder) {
        super(holder, holder.value().getIngredientsList(), EmiStack.of(holder.value().getResultItem()));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.ALLOY_SMELTING_CATEGORY;
    }

    @Override
    protected int getGridColumns() {
        return 2;
    }

    @Override
    protected int getCookingTime() {
        return recipe.getCookingTime();
    }

    @Override
    protected float getExperience() {
        return recipe.getExperience();
    }
}
