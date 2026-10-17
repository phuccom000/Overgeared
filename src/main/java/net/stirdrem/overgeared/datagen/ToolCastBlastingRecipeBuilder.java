package net.stirdrem.overgeared.datagen;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static net.minecraft.data.recipes.RecipeBuilder.ROOT_RECIPE_ADVANCEMENT;
import static net.minecraft.data.recipes.RecipeBuilder.getDefaultRecipeId;

public class ToolCastBlastingRecipeBuilder extends CraftingRecipeBuilder {

    private final ItemLike result;
    private final float experience;
    private final int cookTime;

    private final Map<String, Integer> materialInput = new HashMap<>();
    private final Advancement.Builder advancement =
            Advancement.Builder.recipeAdvancement();

    private String toolType;

    @Nullable
    private Boolean needPolishing = null;

    @Nullable
    private String group = "misc";

    @Nullable
    private String category = "misc";

    public ToolCastBlastingRecipeBuilder(
            ItemLike result,
            float experience,
            int cookTime
    ) {
        this.result = result;
        this.experience = experience;
        this.cookTime = cookTime;
    }

    public static ToolCastBlastingRecipeBuilder cast(
            ItemLike result,
            float xp,
            int time
    ) {
        return new ToolCastBlastingRecipeBuilder(result, xp, time);
    }

    public ToolCastBlastingRecipeBuilder toolType(String type) {
        this.toolType = type;
        return this;
    }

    public ToolCastBlastingRecipeBuilder material(
            String material,
            int amount
    ) {
        this.materialInput.put(material, amount);
        return this;
    }

    public ToolCastBlastingRecipeBuilder needsPolishing(boolean flag) {
        this.needPolishing = flag;
        return this;
    }

    public ToolCastBlastingRecipeBuilder criterion(
            String name,
            CriterionTriggerInstance conditions
    ) {
        this.advancement.addCriterion(name, conditions);
        return this;
    }

    public ToolCastBlastingRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public ToolCastBlastingRecipeBuilder category(String category) {
        this.category = category;
        return this;
    }

    public Item getOutputItem() {
        return result.asItem();
    }

    public void offerTo(Consumer<FinishedRecipe> exporter) {
        offerTo(exporter, getDefaultRecipeId(this.getOutputItem()));
    }

    public void offerTo(
            Consumer<FinishedRecipe> exporter,
            ResourceLocation id
    ) {
        ensureValid(id);

        ResourceLocation recipeId = new ResourceLocation(
                id.getNamespace(),
                id.getPath() + "_from_cast_blasting"
        );

        this.advancement
                .parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion(
                        "has_the_recipe",
                        RecipeUnlockedTrigger.unlocked(id)
                )
                .rewards(
                        AdvancementRewards.Builder.recipe(id)
                )
                .requirements(RequirementsStrategy.OR);

        exporter.accept(new Result(
                recipeId,
                result,
                group,
                category,
                toolType,
                materialInput,
                experience,
                cookTime,
                needPolishing,
                advancement,
                recipeId.withPrefix("recipes/misc/")
        ));
    }

    private void ensureValid(ResourceLocation id) {
        if (toolType == null) {
            throw new IllegalStateException(
                    "Tool type missing for " + id
            );
        }

        if (materialInput.isEmpty()) {
            throw new IllegalStateException(
                    "No material input for " + id
            );
        }

        if (advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException(
                    "No unlock criteria for " + id
            );
        }
    }

    public static class Result implements FinishedRecipe {

        private final ResourceLocation id;
        private final ItemLike result;
        private final String group;
        private final String category;
        private final String toolType;
        private final Map<String, Integer> input;
        private final float xp;
        private final int time;
        private final Boolean needPolishing;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(
                ResourceLocation id,
                ItemLike result,
                String group,
                String category,
                String toolType,
                Map<String, Integer> input,
                float xp,
                int time,
                Boolean needPolishing,
                Advancement.Builder advancement,
                ResourceLocation advancementId
        ) {
            this.id = id;
            this.result = result;
            this.group = group;
            this.category = category;
            this.toolType = toolType;
            this.input = input;
            this.xp = xp;
            this.time = time;
            this.needPolishing = needPolishing;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (group != null && !group.isEmpty()) {
                json.addProperty("group", group);
            }

            if (category != null) {
                json.addProperty("category", category);
            }

            json.addProperty("tool_type", toolType);

            JsonObject inputObj = new JsonObject();

            input.forEach((material, amount) ->
                    inputObj.add(material, new JsonPrimitive(amount))
            );

            json.add("input", inputObj);

            JsonObject resultObj = new JsonObject();
            resultObj.addProperty(
                    "item",
                    BuiltInRegistries.ITEM.getKey(result.asItem()).toString()
            );

            json.add("result", resultObj);

            if (needPolishing != null) {
                json.addProperty(
                        "need_polishing",
                        needPolishing
                );
            }

            json.addProperty("experience", xp);
            json.addProperty("cookingtime", time);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ModRecipes.CAST_BLASTING;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return advancementId;
        }
    }
}