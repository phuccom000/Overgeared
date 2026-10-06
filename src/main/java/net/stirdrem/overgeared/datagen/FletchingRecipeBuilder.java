package net.stirdrem.overgeared.datagen;

import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.FletchingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** Datagen builder for {@code overgeared:fletching} recipes. */
public class FletchingRecipeBuilder {

    @Nullable
    private final Ingredient tip;
    @Nullable
    private final Ingredient shaft;
    @Nullable
    private final Ingredient feather;
    private final ItemStackTemplate result;

    @Nullable
    private FletchingRecipe.TaggedResult resultTipped;
    @Nullable
    private FletchingRecipe.TaggedResult resultLingering;

    private final RecipeBuilderSupport.Unlocks unlocks = new RecipeBuilderSupport.Unlocks();

    @Nullable
    private String group;

    public FletchingRecipeBuilder(@Nullable Ingredient tip, @Nullable Ingredient shaft, @Nullable Ingredient feather, ItemStackTemplate result) {
        this.tip = tip;
        this.shaft = shaft;
        this.feather = feather;
        this.result = result;
    }

    public static FletchingRecipeBuilder fletching(@Nullable Ingredient tip, @Nullable Ingredient shaft, @Nullable Ingredient feather, ItemLike result) {
        return fletching(tip, shaft, feather, result, 1);
    }

    public static FletchingRecipeBuilder fletching(@Nullable Ingredient tip, @Nullable Ingredient shaft, @Nullable Ingredient feather, ItemLike result, int count) {
        return new FletchingRecipeBuilder(tip, shaft, feather, new ItemStackTemplate(result.asItem(), count));
    }

    public FletchingRecipeBuilder withTippedResult(ItemLike result) {
        return withTippedResult(result, this.result.count());
    }

    public FletchingRecipeBuilder withTippedResult(ItemLike result, int count) {
        return withTippedResult(FletchingRecipe.DEFAULT_TIPPED_TAG, result, count);
    }

    public FletchingRecipeBuilder withTippedResult(String tag, ItemLike result, int count) {
        this.resultTipped = new FletchingRecipe.TaggedResult(new ItemStackTemplate(result.asItem(), count), Optional.ofNullable(tag));
        return this;
    }

    public FletchingRecipeBuilder withTippedResult(String tag, ItemLike result) {
        return withTippedResult(tag, result, this.result.count());
    }

    public FletchingRecipeBuilder withLingeringResult(ItemLike result) {
        return withLingeringResult(result, this.result.count());
    }

    public FletchingRecipeBuilder withLingeringResult(ItemLike result, int count) {
        return withLingeringResult(FletchingRecipe.DEFAULT_LINGERING_TAG, result, count);
    }

    public FletchingRecipeBuilder withLingeringResult(String tag, ItemLike result, int count) {
        this.resultLingering = new FletchingRecipe.TaggedResult(new ItemStackTemplate(result.asItem(), count), Optional.ofNullable(tag));
        return this;
    }

    public FletchingRecipeBuilder withLingeringResult(String tag, ItemLike result) {
        return withLingeringResult(tag, result, this.result.count());
    }

    public FletchingRecipeBuilder criterion(String name, Criterion<?> conditions) {
        unlocks.add(name, conditions);
        return this;
    }

    public FletchingRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public Item getOutputItem() {
        return result.item().value();
    }

    public void offerTo(RecipeOutput output) {
        offerTo(output, RecipeBuilderSupport.defaultId(getOutputItem()));
    }

    public void offerTo(RecipeOutput output, Identifier recipeId) {
        FletchingRecipe recipe = new FletchingRecipe(
                group == null ? "" : group,
                Optional.ofNullable(tip), Optional.ofNullable(shaft), Optional.ofNullable(feather),
                Optional.empty(),
                result,
                Optional.ofNullable(resultTipped),
                Optional.ofNullable(resultLingering));
        ResourceKey<Recipe<?>> id = RecipeBuilderSupport.key(recipeId);
        output.accept(id, recipe, unlocks.build(output, id, "fletching"));
    }
}
