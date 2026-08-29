package net.stirdrem.overgeared.datagen;

import com.google.gson.JsonObject;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.criterion.CriterionConditions;
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

public class CastingRecipeBuilder extends RecipeJsonBuilder {

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
    private String group = "";

    @Nullable
    private String category = "misc";

    private CastingRecipeBuilder(
            ItemConvertible result,
            float xp,
            int cookTime
    ) {
        this.result = result;
        this.experience = xp;
        this.cookTime = cookTime;
    }

    public static CastingRecipeBuilder casting(
            ItemConvertible result,
            float xp,
            int cookTime
    ) {
        return new CastingRecipeBuilder(result, xp, cookTime);
    }

    public CastingRecipeBuilder toolType(String type) {
        this.toolType = type;
        return this;
    }

    public CastingRecipeBuilder material(String material, int amount) {
        this.materialInput.put(material, amount);
        return this;
    }

    public CastingRecipeBuilder needsPolishing(boolean flag) {
        this.needPolishing = flag;
        return this;
    }

    public CastingRecipeBuilder criterion(
            String name,
            CriterionConditions conditions
    ) {
        this.advancementBuilder().criterion(name, conditions);
        return this;
    }

    private Advancement.Builder advancementBuilder() {
        return this.advancement;
    }

    public CastingRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public CastingRecipeBuilder category(String category) {
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
                id.getPath() + "_from_cast_furnace"
        );

        this.advancement
                .parent(ROOT)
                .criterion(
                        "has_the_recipe",
                        net.minecraft.advancement.criterion.RecipeUnlockedCriterion.create(id)
                )
                .rewards(
                        net.minecraft.advancement.AdvancementRewards.Builder.recipe(id)
                )
                .criteriaMerger(
                        net.minecraft.advancement.CriterionMerger.OR
                );

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
                recipeId.withPrefixedPath("recipes/casting/")
        ));
    }

    private void ensureValid(Identifier id) {
        if (toolType == null) {
            throw new IllegalStateException(
                    "Missing tool_type for casting recipe " + id
            );
        }

        if (materialInput.isEmpty()) {
            throw new IllegalStateException(
                    "No material input defined for " + id
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
        private final int cookTime;
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
                int cookTime,
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
            this.cookTime = cookTime;
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
            input.forEach(inputObj::addProperty);
            json.add("input", inputObj);

            JsonObject resultObj = new JsonObject();
            resultObj.addProperty(
                    "item",
                    Registries.ITEM.getId(result.asItem()).toString()
            );
            json.add("result", resultObj);

            if (needPolishing != null) {
                json.addProperty("need_polishing", needPolishing);
            }

            json.addProperty("experience", xp);
            json.addProperty("cookingtime", cookTime);
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return ModRecipes.CASTING;
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