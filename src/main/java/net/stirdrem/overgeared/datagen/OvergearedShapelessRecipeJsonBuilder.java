package net.stirdrem.overgeared.datagen;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class OvergearedShapelessRecipeJsonBuilder
        extends CraftingRecipeBuilder
        implements RecipeBuilder {

    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final List<Ingredient> inputs = Lists.newArrayList();
    private final Advancement.Builder advancementBuilder =
            Advancement.Builder.recipeAdvancement();

    @Nullable
    private String group;

    public OvergearedShapelessRecipeJsonBuilder(
            RecipeCategory category,
            ItemLike output,
            int count
    ) {
        this.category = category;
        this.output = output.asItem();
        this.count = count;
    }

    public static OvergearedShapelessRecipeJsonBuilder create(
            RecipeCategory category,
            ItemLike output
    ) {
        return new OvergearedShapelessRecipeJsonBuilder(category, output, 1);
    }

    public static OvergearedShapelessRecipeJsonBuilder create(
            RecipeCategory category,
            ItemLike output,
            int count
    ) {
        return new OvergearedShapelessRecipeJsonBuilder(category, output, count);
    }

    public static OvergearedShapelessRecipeJsonBuilder shapeless(
            RecipeCategory category,
            ItemLike output
    ) {
        return create(category, output);
    }

    public static OvergearedShapelessRecipeJsonBuilder shapeless(
            RecipeCategory category,
            ItemLike output,
            int count
    ) {
        return create(category, output, count);
    }

    public OvergearedShapelessRecipeJsonBuilder requires(
            TagKey<Item> tag
    ) {
        return input(Ingredient.of(tag));
    }

    public OvergearedShapelessRecipeJsonBuilder requires(
            ItemLike item
    ) {
        return input(item, 1);
    }

    public OvergearedShapelessRecipeJsonBuilder requires(
            ItemLike item,
            int size
    ) {
        return input(Ingredient.of(item), size);
    }

    public OvergearedShapelessRecipeJsonBuilder requires(
            Ingredient ingredient
    ) {
        return input(ingredient, 1);
    }

    public OvergearedShapelessRecipeJsonBuilder requires(
            Ingredient ingredient,
            int size
    ) {
        for (int i = 0; i < size; ++i) {
            this.inputs.add(ingredient);
        }

        return this;
    }

    public OvergearedShapelessRecipeJsonBuilder input(
            TagKey<Item> tag
    ) {
        return input(Ingredient.of(tag));
    }

    public OvergearedShapelessRecipeJsonBuilder input(
            ItemLike item
    ) {
        return input(item, 1);
    }

    public OvergearedShapelessRecipeJsonBuilder input(
            ItemLike item,
            int size
    ) {
        for (int i = 0; i < size; ++i) {
            this.inputs.add(Ingredient.of(item));
        }

        return this;
    }

    public OvergearedShapelessRecipeJsonBuilder input(
            Ingredient ingredient
    ) {
        return input(ingredient, 1);
    }

    public OvergearedShapelessRecipeJsonBuilder input(
            Ingredient ingredient,
            int size
    ) {
        for (int i = 0; i < size; ++i) {
            this.inputs.add(ingredient);
        }

        return this;
    }

    public OvergearedShapelessRecipeJsonBuilder unlockedBy(
            String name,
            CriterionTriggerInstance conditions
    ) {
        this.advancementBuilder.addCriterion(name, conditions);
        return this;
    }

    public OvergearedShapelessRecipeJsonBuilder group(
            @Nullable String group
    ) {
        this.group = group;
        return this;
    }

    @Override
    public Item getResult() {
        return this.output;
    }

    @Override
    public void save(
            Consumer<FinishedRecipe> exporter,
            ResourceLocation recipeId
    ) {
        this.validate(recipeId);

        this.advancementBuilder
                .parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion(
                        "has_the_recipe",
                        RecipeUnlockedTrigger.unlocked(recipeId)
                )
                .rewards(
                        net.minecraft.advancements.AdvancementRewards.Builder.recipe(recipeId)
                )
                .requirements(RequirementsStrategy.OR);

        exporter.accept(
                new OvergearedShapelessRecipeJsonProvider(
                        recipeId,
                        this.output,
                        this.count,
                        this.group == null ? "" : this.group,
                        determineBookCategory(this.category),
                        this.inputs,
                        this.advancementBuilder,
                        recipeId.withPrefix(
                                "recipes/" + this.category.getFolderName() + "/"
                        )
                )
        );
    }

    private void validate(ResourceLocation recipeId) {
        if (this.advancementBuilder.getCriteria().isEmpty()) {
            throw new IllegalStateException(
                    "No way of obtaining recipe " + recipeId
            );
        }
    }

    public static class OvergearedShapelessRecipeJsonProvider
            extends CraftingResult {

        private final ResourceLocation recipeId;
        private final Item output;
        private final int count;
        private final String group;
        private final List<Ingredient> inputs;
        private final Advancement.Builder advancementBuilder;
        private final ResourceLocation advancementId;

        public OvergearedShapelessRecipeJsonProvider(
                ResourceLocation recipeId,
                Item output,
                int outputCount,
                String group,
                CraftingBookCategory craftingCategory,
                List<Ingredient> inputs,
                Advancement.Builder advancementBuilder,
                ResourceLocation advancementId
        ) {
            super(craftingCategory);
            this.recipeId = recipeId;
            this.output = output;
            this.count = outputCount;
            this.group = group;
            this.inputs = inputs;
            this.advancementBuilder = advancementBuilder;
            this.advancementId = advancementId;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            super.serializeRecipeData(json);

            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            JsonArray jsonArray = new JsonArray();

            for (Ingredient ingredient : this.inputs) {
                jsonArray.add(ingredient.toJson());
            }

            json.add("ingredients", jsonArray);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(
                    "item",
                    BuiltInRegistries.ITEM.getKey(this.output).toString()
            );

            if (this.count > 1) {
                jsonObject.addProperty("count", this.count);
            }

            json.add("result", jsonObject);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ModRecipes.CRAFTING_SHAPELESS;
        }

        @Override
        public ResourceLocation getId() {
            return this.recipeId;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return this.advancementBuilder.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}