package net.stirdrem.overgeared.datagen;

import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.OvergearedShapelessRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Datagen builder for {@code overgeared:crafting_shapeless} recipes. */
public class OvergearedShapelessRecipeJsonBuilder implements RecipeBuilder {

    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final List<Function<HolderGetter<Item>, Ingredient>> inputs = new ArrayList<>();
    private final RecipeBuilderSupport.Unlocks unlocks = new RecipeBuilderSupport.Unlocks();

    @Nullable
    private String group;

    public OvergearedShapelessRecipeJsonBuilder(RecipeCategory category, ItemLike output, int count) {
        this.category = category;
        this.output = output.asItem();
        this.count = count;
    }

    public static OvergearedShapelessRecipeJsonBuilder create(RecipeCategory category, ItemLike output) {
        return new OvergearedShapelessRecipeJsonBuilder(category, output, 1);
    }

    public static OvergearedShapelessRecipeJsonBuilder create(RecipeCategory category, ItemLike output, int count) {
        return new OvergearedShapelessRecipeJsonBuilder(category, output, count);
    }

    public static OvergearedShapelessRecipeJsonBuilder shapeless(RecipeCategory category, ItemLike output) {
        return create(category, output);
    }

    public static OvergearedShapelessRecipeJsonBuilder shapeless(RecipeCategory category, ItemLike output, int count) {
        return create(category, output, count);
    }

    public OvergearedShapelessRecipeJsonBuilder requires(TagKey<Item> tag) {
        return input(tag);
    }

    public OvergearedShapelessRecipeJsonBuilder requires(ItemLike item) {
        return input(item, 1);
    }

    public OvergearedShapelessRecipeJsonBuilder requires(ItemLike item, int size) {
        return input(item, size);
    }

    public OvergearedShapelessRecipeJsonBuilder requires(Ingredient ingredient) {
        return input(ingredient, 1);
    }

    public OvergearedShapelessRecipeJsonBuilder requires(Ingredient ingredient, int size) {
        return input(ingredient, size);
    }

    public OvergearedShapelessRecipeJsonBuilder input(TagKey<Item> tag) {
        inputs.add(RecipeBuilderSupport.lazy(tag));
        return this;
    }

    public OvergearedShapelessRecipeJsonBuilder input(ItemLike item) {
        return input(item, 1);
    }

    public OvergearedShapelessRecipeJsonBuilder input(ItemLike item, int size) {
        return input(Ingredient.of(item), size);
    }

    public OvergearedShapelessRecipeJsonBuilder input(Ingredient ingredient) {
        return input(ingredient, 1);
    }

    public OvergearedShapelessRecipeJsonBuilder input(Ingredient ingredient, int size) {
        for (int i = 0; i < size; ++i) {
            inputs.add(RecipeBuilderSupport.lazy(ingredient));
        }
        return this;
    }

    @Override
    public OvergearedShapelessRecipeJsonBuilder unlockedBy(String name, Criterion<?> criterion) {
        unlocks.add(name, criterion);
        return this;
    }

    @Override
    public OvergearedShapelessRecipeJsonBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilderSupport.key(RecipeBuilderSupport.defaultId(output));
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
        HolderGetter<Item> items = RecipeBuilderSupport.items(recipeOutput);
        List<OvergearedShapelessRecipe.IngredientWithRemainder> ingredients = inputs.stream()
                .map(f -> OvergearedShapelessRecipe.IngredientWithRemainder.of(f.apply(items)))
                .toList();
        OvergearedShapelessRecipe recipe = new OvergearedShapelessRecipe(
                RecipeBuilder.createCraftingCommonInfo(true),
                RecipeBuilder.createCraftingBookInfo(category, group),
                new ItemStackTemplate(output, count),
                ingredients);
        recipeOutput.accept(id, recipe, unlocks.build(recipeOutput, id, category.getFolderName()));
    }
}
