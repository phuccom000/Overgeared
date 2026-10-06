package net.stirdrem.overgeared.recipe;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.stirdrem.overgeared.Overgeared;

/**
 * Recipe book categories for Overgeared's own recipe types. Since 1.21.2 every {@code Recipe} must
 * report one. None of our types provide vanilla recipe displays, so these never show up in the
 * vanilla recipe book; they only exist to satisfy the API.
 */
public final class ModRecipeBookCategories {
    public static final RecipeBookCategory FORGING = register("forging");
    public static final RecipeBookCategory KNAPPING = register("rock_knapping");
    public static final RecipeBookCategory FLETCHING = register("fletching");
    public static final RecipeBookCategory ALLOY_SMELTING = register("alloy_smelting");
    public static final RecipeBookCategory NETHER_ALLOY_SMELTING = register("nether_alloy_smelting");
    public static final RecipeBookCategory COOLING = register("cooling");
    public static final RecipeBookCategory GRINDING = register("grinding");
    public static final RecipeBookCategory CASTING = register("casting");
    public static final RecipeBookCategory ITEM_TO_TOOLTYPE = register("item_to_tooltype");

    private ModRecipeBookCategories() {
    }

    private static RecipeBookCategory register(String name) {
        return Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, Overgeared.id(name), new RecipeBookCategory());
    }

    public static void register() {
    }
}
