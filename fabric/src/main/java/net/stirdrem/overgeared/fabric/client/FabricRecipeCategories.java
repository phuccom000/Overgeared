package net.stirdrem.overgeared.fabric.client;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.client.ForgingBookCategory;
import net.stirdrem.overgeared.recipe.ForgingRecipe;

import java.util.List;

/**
 * The forging recipe book tabs, created by {@code RecipeBookCategoriesMixin}.
 * Mirrors the NeoForge {@code RecipeCategories} registration.
 */
public final class FabricRecipeCategories {
    private static RecipeBookCategories search;
    private static RecipeBookCategories tools;
    private static RecipeBookCategories armors;
    private static RecipeBookCategories misc;

    private FabricRecipeCategories() {
    }

    public static void init(RecipeBookCategories search, RecipeBookCategories tools,
                            RecipeBookCategories armors, RecipeBookCategories misc) {
        FabricRecipeCategories.search = search;
        FabricRecipeCategories.tools = tools;
        FabricRecipeCategories.armors = armors;
        FabricRecipeCategories.misc = misc;
    }

    public static List<RecipeBookCategories> forgingCategories() {
        return List.of(search, tools, armors, misc);
    }

    public static RecipeBookCategories categoryFor(RecipeHolder<?> recipe) {
        if (recipe.value() instanceof ForgingRecipe forgingRecipe) {
            ForgingBookCategory tab = forgingRecipe.getRecipeBookTab();
            if (tab != null) {
                return switch (tab) {
                    case TOOL_HEADS -> tools;
                    case ARMORS -> armors;
                    case MISC -> misc;
                };
            }
        }
        return misc;
    }
}
