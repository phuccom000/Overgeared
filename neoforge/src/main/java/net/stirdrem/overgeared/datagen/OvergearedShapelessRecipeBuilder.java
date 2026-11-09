package net.stirdrem.overgeared.datagen;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import com.google.common.collect.Lists;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.OvergearedShapelessRecipe;
import net.stirdrem.overgeared.recipe.ingredients.IngredientWithRemainder;

import javax.annotation.Nullable;
import java.util.*;

public class OvergearedShapelessRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final List<IngredientWithRemainder> ingredients = Lists.newArrayList();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    public OvergearedShapelessRecipeBuilder(RecipeCategory pCategory, ItemLike pResult, int pCount) {
        this.category = pCategory;
        this.result = pResult.asItem();
        this.count = pCount;
    }

    public static OvergearedShapelessRecipeBuilder shapeless(RecipeCategory pCategory, ItemLike pResult) {
        return new OvergearedShapelessRecipeBuilder(pCategory, pResult, 1);
    }

    public static OvergearedShapelessRecipeBuilder shapeless(RecipeCategory pCategory, ItemLike pResult, int pCount) {
        return new OvergearedShapelessRecipeBuilder(pCategory, pResult, pCount);
    }

    public OvergearedShapelessRecipeBuilder requires(TagKey<Item> pTag) {
        return this.requires(DatagenIngredients.tag(pTag));
    }

    public OvergearedShapelessRecipeBuilder requires(ItemLike pItem) {
        return this.requires(pItem, 1);
    }

    public OvergearedShapelessRecipeBuilder requires(ItemLike pItem, int pQuantity) {
        for (int i = 0; i < pQuantity; ++i) {
            this.requires(Ingredient.of(pItem));
        }
        return this;
    }

    public OvergearedShapelessRecipeBuilder requires(Ingredient pIngredient) {
        return this.requires(pIngredient, 1);
    }

    public OvergearedShapelessRecipeBuilder requires(Ingredient pIngredient, int pQuantity) {
        for (int i = 0; i < pQuantity; ++i) {
            this.ingredients.add(new IngredientWithRemainder(pIngredient, false, 0));
        }
        return this;
    }

    @Override
    public OvergearedShapelessRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public OvergearedShapelessRecipeBuilder group(@Nullable String pGroupName) {
        this.group = pGroupName;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(this.result));
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

        // Ingredients
        NonNullList<IngredientWithRemainder> ingredients = NonNullList.create();
        ingredients.addAll(this.ingredients);

        // Default remainder & durability arrays (all false / 0)
        boolean[] remainder = new boolean[ingredients.size()];
        int[] durability = new int[ingredients.size()];

        // Construct recipe using NEW format
        OvergearedShapelessRecipe recipe = new OvergearedShapelessRecipe(
                this.group == null ? "" : this.group,
                determineBookCategory(this.category),
                new ItemStackTemplate(this.result, this.count),
                ingredients
        );

        output.accept(
                recipeIdKey,
                recipe,
                advBuilder.build(
                        recipeId.withPrefix("recipes/" + this.category.getFolderName() + "/")
                )
        );
    }

    private void ensureValid(Identifier pId) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + pId);
        }
    }

    private static CraftingBookCategory determineBookCategory(RecipeCategory category) {
        return switch (category) {
            case BUILDING_BLOCKS -> CraftingBookCategory.BUILDING;
            case TOOLS, COMBAT -> CraftingBookCategory.EQUIPMENT;
            case REDSTONE -> CraftingBookCategory.REDSTONE;
            default -> CraftingBookCategory.MISC;
        };
    }
}