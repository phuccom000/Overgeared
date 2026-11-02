package net.stirdrem.overgeared.recipebook;

import net.minecraft.client.RecipeBookCategories;
import net.stirdrem.overgeared.client.ForgingBookCategory;
import net.stirdrem.overgeared.recipe.ForgingRecipe;

import java.util.List;

/**
 * Smithing-anvil recipe-book tabs on Fabric, appended to the vanilla enum by {@code RecipeBookCategoriesMixin}.
 * Mirrors Forge's {@code ModRecipeBookTypes}: a search tab aggregating tools, armors and misc.
 */
public final class FabricRecipeBookCategories {
    public static RecipeBookCategories SEARCH;
    public static RecipeBookCategories TOOLS;
    public static RecipeBookCategories ARMORS;
    public static RecipeBookCategories MISC;
    public static List<RecipeBookCategories> ALL = List.of();

    private FabricRecipeBookCategories() {
    }

    /** Called once from the mixin while {@link RecipeBookCategories} initialises. */
    public static void init(RecipeBookCategories search, RecipeBookCategories tools,
                            RecipeBookCategories armors, RecipeBookCategories misc) {
        SEARCH = search;
        TOOLS = tools;
        ARMORS = armors;
        MISC = misc;
        ALL = List.of(search, tools, armors, misc);
    }

    public static RecipeBookCategories forRecipe(ForgingRecipe recipe) {
        ForgingBookCategory tab = recipe.getRecipeBookTab();
        if (tab == null) return MISC;
        return switch (tab) {
            case TOOL_HEADS -> TOOLS;
            case ARMORS -> ARMORS;
            case MISC -> MISC;
        };
    }
}
