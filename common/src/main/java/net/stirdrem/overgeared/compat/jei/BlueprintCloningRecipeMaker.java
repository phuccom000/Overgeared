package net.stirdrem.overgeared.compat.jei;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;

import java.util.ArrayList;
import java.util.List;

/**
 * Display-only crafting entries: blueprint + empty blueprint -> 2 blueprints one quality tier lower.
 */
public final class BlueprintCloningRecipeMaker {

    private BlueprintCloningRecipeMaker() {
    }

    public static List<RecipeHolder<CraftingRecipe>> createRecipes() {
        List<ForgingQuality> qualities = new ArrayList<>(List.of(ForgingQuality.values()));
        qualities.remove(ForgingQuality.NONE);
        qualities.sort((a, b) -> Integer.compare(b.ordinal(), a.ordinal())); // highest quality first

        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
        for (ForgingQuality quality : qualities) {
            ForgingQuality outputQuality = ForgingQuality.getPrevious(quality);
            if (outputQuality == null || outputQuality == ForgingQuality.NONE) {
                outputQuality = quality; // lowest tier stays the same
            }

            ItemStack input = new ItemStack(ModItems.BLUEPRINT.get());
            input.set(ModComponents.BLUEPRINT_DATA.get(), BlueprintData.createDefault().withQuality(quality.getDisplayName()));

            ItemStack output = new ItemStack(ModItems.BLUEPRINT.get(), 2);
            output.set(ModComponents.BLUEPRINT_DATA.get(), BlueprintData.createDefault().withQuality(outputQuality.getDisplayName()));

            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
                    Ingredient.of(input),
                    Ingredient.of(ModItems.EMPTY_BLUEPRINT.get())
            );

            recipes.add(new RecipeHolder<>(
                    OvergearedMod.loc("jei/blueprint_cloning/" + quality.getDisplayName()),
                    new ShapelessRecipe("jei.blueprint.clone", CraftingBookCategory.MISC, output, inputs)
            ));
        }
        return recipes;
    }
}
