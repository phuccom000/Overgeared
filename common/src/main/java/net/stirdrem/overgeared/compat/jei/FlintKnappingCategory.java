package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;

public class FlintKnappingCategory extends ExplanationCategory {
    public static final RecipeType<ExplanationRecipe> FLINT_KNAPPING =
            new RecipeType<>(OvergearedMod.loc("flint_knapping"), ExplanationRecipe.class);

    public FlintKnappingCategory(IGuiHelper guiHelper) {
        super(guiHelper, FLINT_KNAPPING, new ItemStack(Items.FLINT),
                "jei.overgeared.category.flint_knapping",
                "jei.overgeared.flint_knapping.description");
    }

    @Override
    protected Ingredient getInput() {
        return Ingredient.of(Items.FLINT);
    }
}
