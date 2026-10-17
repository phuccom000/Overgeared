package net.stirdrem.overgeared.datagen;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.FletchingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static net.minecraft.data.recipes.RecipeBuilder.ROOT_RECIPE_ADVANCEMENT;
import static net.minecraft.data.recipes.RecipeBuilder.getDefaultRecipeId;

public class FletchingRecipeBuilder extends CraftingRecipeBuilder {

    private final Ingredient tip;
    private final Ingredient shaft;
    private final Ingredient feather;
    private final ItemStack result;

    private ItemStack resultTipped = ItemStack.EMPTY;
    private String tippedTag = null;

    private ItemStack resultLingering = ItemStack.EMPTY;
    private String lingeringTag = null;

    private final Advancement.Builder advancement =
            Advancement.Builder.recipeAdvancement();

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
            ItemLike result
    ) {
        return fletching(tip, shaft, feather, result, 1);
    }

    public static FletchingRecipeBuilder fletching(
            Ingredient tip,
            Ingredient shaft,
            Ingredient feather,
            ItemLike result,
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
            ItemLike result
    ) {
        return withTippedResult(result, this.result.getCount());
    }

    public FletchingRecipeBuilder withTippedResult(
            ItemLike result,
            int count
    ) {
        return withTippedResult("Potion", result, count);
    }

    public FletchingRecipeBuilder withTippedResult(
            String tag,
            ItemLike result,
            int count
    ) {
        this.resultTipped = new ItemStack(result, count);
        this.tippedTag = tag;
        return this;
    }

    public FletchingRecipeBuilder withTippedResult(
            String tag,
            ItemLike result
    ) {
        this.resultTipped = new ItemStack(result, this.result.getCount());
        this.tippedTag = tag;
        return this;
    }

    public FletchingRecipeBuilder withLingeringResult(
            ItemLike result
    ) {
        return withLingeringResult(result, this.result.getCount());
    }

    public FletchingRecipeBuilder withLingeringResult(
            ItemLike result,
            int count
    ) {
        return withLingeringResult("LingeringPotion", result, count);
    }

    public FletchingRecipeBuilder withLingeringResult(
            String tag,
            ItemLike result,
            int count
    ) {
        this.resultLingering = new ItemStack(result, count);
        this.lingeringTag = tag;
        return this;
    }

    public FletchingRecipeBuilder withLingeringResult(
            String tag,
            ItemLike result
    ) {
        this.resultLingering = new ItemStack(result, this.result.getCount());
        this.lingeringTag = tag;
        return this;
    }

    public FletchingRecipeBuilder criterion(
            String name,
            CriterionTriggerInstance conditions
    ) {
        this.advancement.addCriterion(name, conditions);
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

    public void offerTo(Consumer<FinishedRecipe> exporter) {
        offerTo(exporter, getDefaultRecipeId(this.getOutputItem()));
    }

    public void offerTo(
            Consumer<FinishedRecipe> exporter,
            ResourceLocation recipeId
    ) {
        ensureValid(recipeId);

        this.advancement
                .parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion(
                        "has_the_recipe",
                        RecipeUnlockedTrigger.unlocked(recipeId)
                )
                .rewards(
                        AdvancementRewards.Builder.recipe(recipeId)
                )
                .requirements(RequirementsStrategy.OR);

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
                recipeId.withPrefix("recipes/fletching/")
        ));
    }

    private void ensureValid(ResourceLocation recipeId) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException(
                    "No way of obtaining recipe " + recipeId
            );
        }
    }

    public static class Result implements FinishedRecipe {

        private final ResourceLocation id;
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
        private final ResourceLocation advancementId;

        public Result(
                ResourceLocation id,
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
                ResourceLocation advancementId
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
        public void serializeRecipeData(JsonObject json) {
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
                    BuiltInRegistries.ITEM
                            .getKey(this.result.getItem())
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
                        BuiltInRegistries.ITEM
                                .getKey(this.resultTipped.getItem())
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
                        BuiltInRegistries.ITEM
                                .getKey(this.resultLingering.getItem())
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
        public RecipeSerializer<?> getType() {
            return FletchingRecipe.Serializer.INSTANCE;
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}