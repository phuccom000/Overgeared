package net.stirdrem.overgeared.client;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

import java.util.function.Supplier;

public class ModRecipeBookTypes {

    private static final Supplier<RecipeBookCategories> SEARCH_CATEGORY = Suppliers.memoize(
            () -> RecipeBookCategories.create("FORGING_SEARCH", new ItemStack(Items.COMPASS)));
    private static final Supplier<RecipeBookCategories> FORGING_TOOLS = Suppliers.memoize(
            () -> RecipeBookCategories.create("FORGING_TOOLS", new ItemStack(ModItems.IRON_PICKAXE_HEAD.get())));
    private static final Supplier<RecipeBookCategories> FORGING_ARMORS = Suppliers.memoize(
            () -> RecipeBookCategories.create("FORGING_ARMORS", new ItemStack(Items.IRON_CHESTPLATE)));
    private static final Supplier<RecipeBookCategories> FORGING_MISC = Suppliers.memoize(
            () -> RecipeBookCategories.create("FORGING_MISC", new ItemStack(Items.ANVIL)));

    public static void init(RegisterRecipeBookCategoriesEvent event) {
        event.registerBookCategories(OvergearedMod.FORGING, ImmutableList.of(SEARCH_CATEGORY.get(), FORGING_TOOLS.get(), FORGING_ARMORS.get(), FORGING_MISC.get()));
        event.registerAggregateCategory(SEARCH_CATEGORY.get(), ImmutableList.of(FORGING_TOOLS.get(), FORGING_ARMORS.get(), FORGING_MISC.get()));
        event.registerRecipeCategoryFinder(ModRecipeTypes.FORGING.get(), recipe ->
        {
            if (recipe instanceof ForgingRecipe cookingRecipe) {
                ForgingBookCategory tab = cookingRecipe.getRecipeBookTab();
                if (tab != null) {
                    return switch (tab) {
                        case TOOL_HEADS -> FORGING_TOOLS.get();
                        case ARMORS -> FORGING_ARMORS.get();
                        case MISC -> FORGING_MISC.get();
                    };
                }
            }
            return FORGING_MISC.get();
        });
    }
}