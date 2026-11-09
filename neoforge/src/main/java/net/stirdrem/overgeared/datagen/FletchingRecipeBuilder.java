package net.stirdrem.overgeared.datagen;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.FletchingRecipe;

import javax.annotation.Nullable;
import java.util.*;

public class FletchingRecipeBuilder implements RecipeBuilder {
    private final Ingredient tip;
    private final Ingredient shaft;
    private final Ingredient feather;
    private final ItemStackTemplate result;
    private ItemStackTemplate resultTipped;
    private String tippedTag = null;
    private ItemStackTemplate resultLingering;
    private String lingeringTag = null;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private String group;

    public FletchingRecipeBuilder(Ingredient tip, Ingredient shaft, Ingredient feather, ItemStackTemplate result) {
        this.tip = tip;
        this.shaft = shaft;
        this.feather = feather;
        this.result = result;
    }

    public static FletchingRecipeBuilder fletching(Ingredient tip, Ingredient shaft, Ingredient feather, ItemLike result) {
        return fletching(tip, shaft, feather, result, 1);
    }

    public static FletchingRecipeBuilder fletching(Ingredient tip, Ingredient shaft, Ingredient feather, ItemLike result, int count) {
        return new FletchingRecipeBuilder(tip, shaft, feather, new ItemStackTemplate(result.asItem(), count));
    }

    // Basic result methods
    public FletchingRecipeBuilder withTippedResult(ItemLike result) {
        return withTippedResult(result, this.result.count());
    }

    public FletchingRecipeBuilder withTippedResult(ItemLike result, int count) {
        return withTippedResult("Potion", result, count);
    }

    public FletchingRecipeBuilder withTippedResult(String tag, ItemLike result, int count) {
        this.resultTipped = new ItemStackTemplate(result.asItem(), count);
        this.tippedTag = tag;
        return this;
    }

    public FletchingRecipeBuilder withTippedResult(String tag, ItemLike result) {
        this.resultLingering = new ItemStackTemplate(result.asItem(), this.result.count());
        this.lingeringTag = tag;
        return this;
    }

    public FletchingRecipeBuilder withLingeringResult(ItemLike result) {
        return withLingeringResult(result, this.result.count());
    }

    public FletchingRecipeBuilder withLingeringResult(ItemLike result, int count) {
        return withLingeringResult("LingeringPotion", result, count);
    }

    public FletchingRecipeBuilder withLingeringResult(String tag, ItemLike result, int count) {
        this.resultLingering = new ItemStackTemplate(result.asItem(), count);
        this.lingeringTag = tag;
        return this;
    }

    public FletchingRecipeBuilder withLingeringResult(String tag, ItemLike result) {
        this.resultLingering = new ItemStackTemplate(result.asItem(), this.result.count());
        this.lingeringTag = tag;
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(String criterionName, Criterion<?> criterion) {
        this.criteria.put(criterionName, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(this.result.item().value()));
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> recipeIdKey) {
        Identifier recipeId = recipeIdKey.identifier();
        this.ensureValid(recipeId);
        Advancement.Builder advBuilder = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(output.lookup(Registries.RECIPE).getOrThrow(recipeIdKey)))
                .rewards(AdvancementRewards.Builder.recipe(recipeIdKey))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advBuilder::addCriterion);

        // Create the actual FletchingRecipe instance
        FletchingRecipe recipe = new FletchingRecipe(
                this.tip,
                this.shaft,
                this.feather,
                null, // potion parameter (not used in builder)
                this.result,
                this.resultTipped,
                this.resultLingering,
                this.tippedTag,
                this.lingeringTag
        );
        
        output.accept(recipeIdKey, recipe, advBuilder.build(recipeId.withPrefix("recipes/fletching/")));
    }

    private void ensureValid(Identifier id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}