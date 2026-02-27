package net.stirdrem.overgeared.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.RecipeBookCategories;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

public class RecipeCategories {
    public static RecipeBookCategories SEARCH_CATEGORY = RecipeBookCategories.valueOf("OVERGEARED_FORGING_SEARCH");
    public static RecipeBookCategories FORGING_TOOLS = RecipeBookCategories.valueOf("OVERGEARED_FORGING_TOOLS");
    public static RecipeBookCategories FORGING_ARMORS = RecipeBookCategories.valueOf("OVERGEARED_FORGING_ARMORS");
    public static RecipeBookCategories FORGING_MISC = RecipeBookCategories.valueOf("OVERGEARED_FORGING_MISC");

    public static void init(RegisterRecipeBookCategoriesEvent event) {
        event.registerBookCategories(OvergearedMod.FORGING, ImmutableList.of(SEARCH_CATEGORY, FORGING_TOOLS, FORGING_ARMORS, FORGING_MISC));
        event.registerAggregateCategory(SEARCH_CATEGORY, ImmutableList.of(FORGING_TOOLS, FORGING_ARMORS, FORGING_MISC));
        event.registerRecipeCategoryFinder(ModRecipeTypes.FORGING.get(), recipe ->
        {
            boolean test = recipe.value() instanceof ForgingRecipe forgingRecipe;
            if (recipe.value() instanceof ForgingRecipe forgingRecipe) {
                ForgingBookCategory tab = forgingRecipe.getRecipeBookTab();
                if (tab != null) {
                    return switch (tab) {
                        case TOOL_HEADS -> FORGING_TOOLS;
                        case ARMORS -> FORGING_ARMORS;
                        case MISC -> FORGING_MISC;
                    };
                }
            }
            return FORGING_MISC;
        });
    }
}
