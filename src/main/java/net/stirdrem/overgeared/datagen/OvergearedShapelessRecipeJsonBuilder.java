package net.stirdrem.overgeared.datagen;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.recipe.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class OvergearedShapelessRecipeJsonBuilder
        extends RecipeJsonBuilder
        implements CraftingRecipeJsonBuilder {

    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final List<Ingredient> inputs = Lists.newArrayList();
    private final Advancement.Builder advancementBuilder =
            Advancement.Builder.createUntelemetered();

    @Nullable
    private String group;

    public OvergearedShapelessRecipeJsonBuilder(
            RecipeCategory category,
            ItemConvertible output,
            int count
    ) {
        this.category = category;
        this.output = output.asItem();
        this.count = count;
    }

    public static OvergearedShapelessRecipeJsonBuilder create(
            RecipeCategory category,
            ItemConvertible output
    ) {
        return new OvergearedShapelessRecipeJsonBuilder(category, output, 1);
    }

    public static OvergearedShapelessRecipeJsonBuilder create(
            RecipeCategory category,
            ItemConvertible output,
            int count
    ) {
        return new OvergearedShapelessRecipeJsonBuilder(category, output, count);
    }

    public static OvergearedShapelessRecipeJsonBuilder shapeless(
            RecipeCategory category,
            ItemConvertible output
    ) {
        return create(category, output);
    }

    public static OvergearedShapelessRecipeJsonBuilder shapeless(
            RecipeCategory category,
            ItemConvertible output,
            int count
    ) {
        return create(category, output, count);
    }

    public OvergearedShapelessRecipeJsonBuilder requires(
            TagKey<Item> tag
    ) {
        return input(Ingredient.fromTag(tag));
    }

    public OvergearedShapelessRecipeJsonBuilder requires(
            ItemConvertible item
    ) {
        return input(item, 1);
    }

    public OvergearedShapelessRecipeJsonBuilder requires(
            ItemConvertible item,
            int size
    ) {
        return input(Ingredient.ofItems(item), size);
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
        return input(Ingredient.fromTag(tag));
    }

    public OvergearedShapelessRecipeJsonBuilder input(
            ItemConvertible item
    ) {
        return input(item, 1);
    }

    public OvergearedShapelessRecipeJsonBuilder input(
            ItemConvertible item,
            int size
    ) {
        for (int i = 0; i < size; ++i) {
            this.inputs.add(Ingredient.ofItems(item));
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

    public OvergearedShapelessRecipeJsonBuilder criterion(
            String name,
            CriterionConditions conditions
    ) {
        this.advancementBuilder.criterion(name, conditions);
        return this;
    }

    public OvergearedShapelessRecipeJsonBuilder group(
            @Nullable String group
    ) {
        this.group = group;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return this.output;
    }

    @Override
    public void offerTo(
            Consumer<RecipeJsonProvider> exporter,
            Identifier recipeId
    ) {
        this.validate(recipeId);

        this.advancementBuilder
                .parent(ROOT)
                .criterion(
                        "has_the_recipe",
                        RecipeUnlockedCriterion.create(recipeId)
                )
                .rewards(
                        net.minecraft.advancement.AdvancementRewards.Builder.recipe(recipeId)
                )
                .criteriaMerger(CriterionMerger.OR);

        exporter.accept(
                new OvergearedShapelessRecipeJsonProvider(
                        recipeId,
                        this.output,
                        this.count,
                        this.group == null ? "" : this.group,
                        getCraftingCategory(this.category),
                        this.inputs,
                        this.advancementBuilder,
                        recipeId.withPrefixedPath(
                                "recipes/" + this.category.getName() + "/"
                        )
                )
        );
    }

    private void validate(Identifier recipeId) {
        if (this.advancementBuilder.getCriteria().isEmpty()) {
            throw new IllegalStateException(
                    "No way of obtaining recipe " + recipeId
            );
        }
    }

    public static class OvergearedShapelessRecipeJsonProvider
            extends CraftingRecipeJsonProvider {

        private final Identifier recipeId;
        private final Item output;
        private final int count;
        private final String group;
        private final List<Ingredient> inputs;
        private final Advancement.Builder advancementBuilder;
        private final Identifier advancementId;

        public OvergearedShapelessRecipeJsonProvider(
                Identifier recipeId,
                Item output,
                int outputCount,
                String group,
                CraftingRecipeCategory craftingCategory,
                List<Ingredient> inputs,
                Advancement.Builder advancementBuilder,
                Identifier advancementId
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
        public void serialize(JsonObject json) {
            super.serialize(json);

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
                    Registries.ITEM.getId(this.output).toString()
            );

            if (this.count > 1) {
                jsonObject.addProperty("count", this.count);
            }

            json.add("result", jsonObject);
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return ModRecipes.CRAFTING_SHAPELESS;
        }

        @Override
        public Identifier getRecipeId() {
            return this.recipeId;
        }

        @Nullable
        @Override
        public JsonObject toAdvancementJson() {
            return this.advancementBuilder.toJson();
        }

        @Nullable
        @Override
        public Identifier getAdvancementId() {
            return this.advancementId;
        }
    }
}