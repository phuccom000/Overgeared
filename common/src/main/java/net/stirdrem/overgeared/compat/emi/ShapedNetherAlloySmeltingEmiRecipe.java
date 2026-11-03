package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.recipe.NetherAlloySmeltingRecipe;
import net.stirdrem.overgeared.recipe.ShapedNetherAlloySmeltingRecipe;

/**
 * EMI recipe display for Nether Alloy Smelting recipes (3x3 input grid).
 */
public class ShapedNetherAlloySmeltingEmiRecipe extends AbstractAlloySmeltingEmiRecipe<ShapedNetherAlloySmeltingRecipe> {

    public ShapedNetherAlloySmeltingEmiRecipe(RecipeHolder<ShapedNetherAlloySmeltingRecipe> holder) {
        super(holder, holder.value().getIngredientsList(), EmiStack.of(holder.value().getResultItem(null)));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.NETHER_ALLOY_SMELTING_CATEGORY;
    }

    @Override
    protected int getGridColumns() {
        return 3;
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
