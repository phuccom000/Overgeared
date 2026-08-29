package net.stirdrem.overgeared.datagen;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.recipe.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder.ROOT;
import static net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder.getItemId;

public class ToolCastBlastingRecipeBuilder extends RecipeJsonBuilder {

    private final ItemConvertible result;
    private final float experience;
    private final int cookTime;

    private final Map<String, Integer> materialInput = new HashMap<>();
    private final Advancement.Builder advancement =
            Advancement.Builder.createUntelemetered();

    private String toolType;

    @Nullable
    private Boolean needPolishing = null;

    @Nullable
    private String group = "misc";

    @Nullable
    private String category = "misc";

    public ToolCastBlastingRecipeBuilder(
            ItemConvertible result,
            float experience,
            int cookTime
    ) {
        this.result = result;
        this.experience = experience;
        this.cookTime = cookTime;
    }

    public static ToolCastBlastingRecipeBuilder cast(
            ItemConvertible result,
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
            CriterionConditions conditions
    ) {
        this.advancement.criterion(name, conditions);
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

    public void offerTo(Consumer<RecipeJsonProvider> exporter) {
        offerTo(exporter, getItemId(this.getOutputItem()));
    }

    public void offerTo(
            Consumer<RecipeJsonProvider> exporter,
            Identifier id
    ) {
        ensureValid(id);

        Identifier recipeId = new Identifier(
                id.getNamespace(),
                id.getPath() + "_from_cast_blasting"
        );

        this.advancement
                .parent(ROOT)
                .criterion(
                        "has_the_recipe",
                        RecipeUnlockedCriterion.create(id)
                )
                .rewards(
                        AdvancementRewards.Builder.recipe(id)
                )
                .criteriaMerger(CriterionMerger.OR);

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
                recipeId.withPrefixedPath("recipes/misc/")
        ));
    }

    private void ensureValid(Identifier id) {
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

    public static class Result implements RecipeJsonProvider {

        private final Identifier id;
        private final ItemConvertible result;
        private final String group;
        private final String category;
        private final String toolType;
        private final Map<String, Integer> input;
        private final float xp;
        private final int time;
        private final Boolean needPolishing;
        private final Advancement.Builder advancement;
        private final Identifier advancementId;

        public Result(
                Identifier id,
                ItemConvertible result,
                String group,
                String category,
                String toolType,
                Map<String, Integer> input,
                float xp,
                int time,
                Boolean needPolishing,
                Advancement.Builder advancement,
                Identifier advancementId
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
        public void serialize(JsonObject json) {
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
                    Registries.ITEM.getId(result.asItem()).toString()
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
        public RecipeSerializer<?> getSerializer() {
            return ModRecipes.CAST_BLASTING;
        }

        @Override
        public Identifier getRecipeId() {
            return id;
        }

        @Nullable
        @Override
        public JsonObject toAdvancementJson() {
            return advancement.toJson();
        }

        @Nullable
        @Override
        public Identifier getAdvancementId() {
            return advancementId;
        }
    }
}