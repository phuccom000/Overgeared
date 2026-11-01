package net.stirdrem.overgeared.compat.jei;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ItemToToolTypeRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Display-only crafting entries for tool casts: a tool-type item surrounded by
 * 4 clay balls (unfired clay cast) or 4 nether bricks (nether cast).
 * Merges the 1.20.1 ClayToolCastRecipeMaker and NetherToolCastRecipeMaker, which were identical apart from those items.
 */
public final class ToolCastRecipeMaker {

    private ToolCastRecipeMaker() {
    }

    public static List<RecipeHolder<CraftingRecipe>> createRecipes(RecipeManager recipeManager, boolean nether) {
        Map<String, List<ItemToToolTypeRecipe>> byToolType = recipeManager
                .getAllRecipesFor(ModRecipeTypes.ITEM_TO_TOOLTYPE.get()).stream()
                .map(RecipeHolder::value)
                .collect(Collectors.groupingBy(ItemToToolTypeRecipe::toolType));

        return byToolType.entrySet().stream()
                .flatMap(entry -> {
                    List<Item> toolItems = entry.getValue().stream()
                            .flatMap(r -> r.getItems().stream())
                            .map(ItemStack::getItem)
                            .filter(item -> item != Items.AIR)
                            .distinct()
                            .toList();
                    return toolItems.isEmpty()
                            ? java.util.stream.Stream.empty()
                            : java.util.stream.Stream.of(createRecipe(toolItems, entry.getKey(), nether));
                })
                .toList();
    }

    private static RecipeHolder<CraftingRecipe> createRecipe(List<Item> toolItems, String toolType, boolean nether) {
        Ingredient center = Ingredient.of(toolItems.stream().map(ItemStack::new));
        Ingredient surround = Ingredient.of(nether ? Items.NETHER_BRICK : Items.CLAY_BALL);
        Ingredient e = Ingredient.EMPTY;

        NonNullList<Ingredient> grid = NonNullList.of(e,
                e, surround, e,
                surround, center, surround,
                e, surround, e
        );

        ItemStack output = new ItemStack(nether ? ModItems.NETHER_TOOL_CAST.get() : ModItems.UNFIRED_TOOL_CAST.get());
        output.set(ModComponents.CAST_DATA, new CastData("", toolType, Map.of(), 0,
                ConfigHelper.getMaxMaterialAmount(toolType), List.of(), ItemStack.EMPTY, false));

        String prefix = nether ? "nether_cast" : "clay_cast";
        return new RecipeHolder<>(
                OvergearedMod.loc("jei/" + prefix + "/" + toolType),
                new ShapedRecipe("jei." + prefix, CraftingBookCategory.MISC,
                        new ShapedRecipePattern(3, 3, grid, Optional.empty()), output)
        );
    }
}
