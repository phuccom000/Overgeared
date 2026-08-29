package net.stirdrem.overgeared.datagen;

import com.google.gson.JsonObject;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.recipe.FletchingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder.ROOT;
import static net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder.getItemId;

public class FletchingRecipeBuilder extends RecipeJsonBuilder {

    private final Ingredient tip;
    private final Ingredient shaft;
    private final Ingredient feather;
    private final ItemStack result;

    private ItemStack resultTipped = ItemStack.EMPTY;
    private String tippedTag = null;

    private ItemStack resultLingering = ItemStack.EMPTY;
    private String lingeringTag = null;

    private final Advancement.Builder advancement =
            Advancement.Builder.createUntelemetered();

    @Nullable
    private String group;

    public FletchingRecipeBuilder(
            Ingredient tip,
            Ingredient shaft,
            Ingredient feather,
            ItemStack result
    ) {
        this.tip = tip;
        this.shaft = shaft;
        this.feather = feather;
        this.result = result;
    }

    public static FletchingRecipeBuilder fletching(
            Ingredient tip,
            Ingredient shaft,
            Ingredient feather,
            ItemConvertible result
    ) {
        return fletching(tip, shaft, feather, result, 1);
    }

    public static FletchingRecipeBuilder fletching(
            Ingredient tip,
            Ingredient shaft,
            Ingredient feather,
            ItemConvertible result,
            int count
    ) {
        return new FletchingRecipeBuilder(
                tip,
                shaft,
                feather,
                new ItemStack(result, count)
        );
    }

    public FletchingRecipeBuilder withTippedResult(
            ItemConvertible result
    ) {
        return withTippedResult(result, this.result.getCount());
    }

    public FletchingRecipeBuilder withTippedResult(
            ItemConvertible result,
            int count
    ) {
        return withTippedResult("Potion", result, count);
    }

    public FletchingRecipeBuilder withTippedResult(
            String tag,
            ItemConvertible result,
            int count
    ) {
        this.resultTipped = new ItemStack(result, count);
        this.tippedTag = tag;
        return this;
    }

    public FletchingRecipeBuilder withTippedResult(
            String tag,
            ItemConvertible result
    ) {
        this.resultTipped = new ItemStack(result, this.result.getCount());
        this.tippedTag = tag;
        return this;
    }

    public FletchingRecipeBuilder withLingeringResult(
            ItemConvertible result
    ) {
        return withLingeringResult(result, this.result.getCount());
    }

    public FletchingRecipeBuilder withLingeringResult(
            ItemConvertible result,
            int count
    ) {
        return withLingeringResult("LingeringPotion", result, count);
    }

    public FletchingRecipeBuilder withLingeringResult(
            String tag,
            ItemConvertible result,
            int count
    ) {
        this.resultLingering = new ItemStack(result, count);
        this.lingeringTag = tag;
        return this;
    }

    public FletchingRecipeBuilder withLingeringResult(
            String tag,
            ItemConvertible result
    ) {
        this.resultLingering = new ItemStack(result, this.result.getCount());
        this.lingeringTag = tag;
        return this;
    }

    public FletchingRecipeBuilder criterion(
            String name,
            CriterionConditions conditions
    ) {
        this.advancement.criterion(name, conditions);
        return this;
    }

    public FletchingRecipeBuilder group(
            @Nullable String group
    ) {
        this.group = group;
        return this;
    }

    public Item getOutputItem() {
        return this.result.getItem();
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter) {
        offerTo(exporter, getItemId(this.getOutputItem()));
    }

    public void offerTo(
            Consumer<RecipeJsonProvider> exporter,
            Identifier recipeId
    ) {
        ensureValid(recipeId);

        this.advancement
                .parent(ROOT)
                .criterion(
                        "has_the_recipe",
                        RecipeUnlockedCriterion.create(recipeId)
                )
                .rewards(
                        AdvancementRewards.Builder.recipe(recipeId)
                )
                .criteriaMerger(CriterionMerger.OR);

        exporter.accept(new Result(
                recipeId,
                this.group == null ? "" : this.group,
                this.tip,
                this.shaft,
                this.feather,
                this.result,
                this.resultTipped,
                this.tippedTag,
                this.resultLingering,
                this.lingeringTag,
                this.advancement,
                recipeId.withPrefixedPath("recipes/fletching/")
        ));
    }

    private void ensureValid(Identifier recipeId) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException(
                    "No way of obtaining recipe " + recipeId
            );
        }
    }

    public static class Result implements RecipeJsonProvider {

        private final Identifier id;
        private final String group;

        private final Ingredient tip;
        private final Ingredient shaft;
        private final Ingredient feather;

        private final ItemStack result;

        private final ItemStack resultTipped;
        private final String tippedTag;

        private final ItemStack resultLingering;
        private final String lingeringTag;

        private final Advancement.Builder advancement;
        private final Identifier advancementId;

        public Result(
                Identifier id,
                String group,
                Ingredient tip,
                Ingredient shaft,
                Ingredient feather,
                ItemStack result,
                ItemStack resultTipped,
                String tippedTag,
                ItemStack resultLingering,
                String lingeringTag,
                Advancement.Builder advancement,
                Identifier advancementId
        ) {
            this.id = id;
            this.group = group;
            this.tip = tip;
            this.shaft = shaft;
            this.feather = feather;
            this.result = result;
            this.resultTipped = resultTipped;
            this.tippedTag = tippedTag;
            this.resultLingering = resultLingering;
            this.lingeringTag = lingeringTag;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        @Override
        public void serialize(JsonObject json) {
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            JsonObject material = new JsonObject();

            material.add("tip", this.tip.toJson());
            material.add("shaft", this.shaft.toJson());
            material.add("feather", this.feather.toJson());

            json.add("material", material);

            JsonObject resultJson = new JsonObject();

            resultJson.addProperty(
                    "item",
                    Registries.ITEM
                            .getId(this.result.getItem())
                            .toString()
            );

            if (this.result.getCount() > 1) {
                resultJson.addProperty(
                        "count",
                        this.result.getCount()
                );
            }

            json.add("result", resultJson);

            if (!this.resultTipped.isEmpty()) {
                JsonObject tippedJson = new JsonObject();

                tippedJson.addProperty(
                        "item",
                        Registries.ITEM
                                .getId(this.resultTipped.getItem())
                                .toString()
                );

                if (this.tippedTag != null) {
                    tippedJson.addProperty(
                            "tag",
                            this.tippedTag
                    );
                }

                if (this.resultTipped.getCount() > 1) {
                    tippedJson.addProperty(
                            "count",
                            this.resultTipped.getCount()
                    );
                }

                json.add("result_tipped", tippedJson);
            }

            if (!this.resultLingering.isEmpty()) {
                JsonObject lingeringJson = new JsonObject();

                lingeringJson.addProperty(
                        "item",
                        Registries.ITEM
                                .getId(this.resultLingering.getItem())
                                .toString()
                );

                if (this.lingeringTag != null) {
                    lingeringJson.addProperty(
                            "tag",
                            this.lingeringTag
                    );
                }

                if (this.resultLingering.getCount() > 1) {
                    lingeringJson.addProperty(
                            "count",
                            this.resultLingering.getCount()
                    );
                }

                json.add(
                        "result_lingering",
                        lingeringJson
                );
            }
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return FletchingRecipe.Serializer.INSTANCE;
        }

        @Override
        public Identifier getRecipeId() {
            return this.id;
        }

        @Nullable
        @Override
        public JsonObject toAdvancementJson() {
            return this.advancement.toJson();
        }

        @Nullable
        @Override
        public Identifier getAdvancementId() {
            return this.advancementId;
        }
    }
}