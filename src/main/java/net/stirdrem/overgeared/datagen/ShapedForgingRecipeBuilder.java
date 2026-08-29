package net.stirdrem.overgeared.datagen;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.recipe.ForgingBookCategory;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder.ROOT;
import static net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder.getItemId;

public class ShapedForgingRecipeBuilder extends RecipeJsonBuilder {

    private final ForgingBookCategory category;
    private final Item result;
    private final int count;
    private final int hammering;

    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();
    private final Advancement.Builder advancement =
            Advancement.Builder.createUntelemetered();

    private final List<String> blueprintTypes = new ArrayList<>();

    @Nullable
    private Boolean requiresBlueprint;

    @Nullable
    private Boolean hasQuality;

    @Nullable
    private Boolean hasPolishing;

    @Nullable
    private Boolean needQuenching;

    @Nullable
    private Boolean needsMinigame;

    @Nullable
    private String group;

    @Nullable
    private String tier;

    @Nullable
    private Item failedResult;

    private int failedResultCount;

    @Nullable
    private ForgingQuality minimumQuality;

    @Nullable
    private ForgingQuality qualityDifficulty;

    private boolean showNotification = true;

    public ShapedForgingRecipeBuilder(
            ForgingBookCategory category,
            ItemConvertible result,
            int count,
            int hammering
    ) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;
        this.hammering = hammering;
    }

    private static boolean isTools(Item item) {
        return item instanceof SwordItem
                || item instanceof MiningToolItem
                || item instanceof RangedWeaponItem;
    }

    public static boolean isToolPart(ItemStack stack) {
        return !stack.isEmpty() && stack.isIn(ModTags.Items.TOOL_PARTS);
    }

    public static boolean isToolPart(Item item) {
        return item.getDefaultStack().isIn(ModTags.Items.TOOL_PARTS);
    }

    private static ForgingBookCategory determineWeaponRecipeCategory(
            ItemConvertible result
    ) {
        Item item = result.asItem();

        if (isTools(item) || isToolPart(item)) {
            return ForgingBookCategory.TOOL_HEADS;
        }

        return item instanceof ArmorItem
                ? ForgingBookCategory.ARMORS
                : ForgingBookCategory.MISC;
    }

    public static ShapedForgingRecipeBuilder create(
            ForgingBookCategory category,
            ItemConvertible result,
            int hammering
    ) {
        return new ShapedForgingRecipeBuilder(
                category,
                result,
                1,
                hammering
        );
    }

    public static ShapedForgingRecipeBuilder create(
            ForgingBookCategory category,
            ItemConvertible result,
            int count,
            int hammering
    ) {
        return new ShapedForgingRecipeBuilder(
                category,
                result,
                count,
                hammering
        );
    }

    public ShapedForgingRecipeBuilder input(
            Character symbol,
            TagKey<Item> tag
    ) {
        return input(symbol, Ingredient.fromTag(tag));
    }

    public ShapedForgingRecipeBuilder input(
            Character symbol,
            ItemConvertible item
    ) {
        return input(symbol, Ingredient.ofItems(item));
    }

    public ShapedForgingRecipeBuilder input(
            Character symbol,
            Ingredient ingredient
    ) {
        if (key.containsKey(symbol)) {
            throw new IllegalArgumentException(
                    "Symbol '" + symbol + "' is already defined!"
            );
        }

        if (symbol == ' ') {
            throw new IllegalArgumentException(
                    "Symbol ' ' (whitespace) is reserved and cannot be defined"
            );
        }

        key.put(symbol, ingredient);
        return this;
    }

    public ShapedForgingRecipeBuilder pattern(String pattern) {
        if (!rows.isEmpty()
                && pattern.length() != rows.get(0).length()) {
            throw new IllegalArgumentException(
                    "Pattern must be the same width on every line!"
            );
        }

        rows.add(pattern);
        return this;
    }

    public ShapedForgingRecipeBuilder criterion(
            String name,
            CriterionConditions conditions
    ) {
        advancement.criterion(name, conditions);
        return this;
    }

    public ShapedForgingRecipeBuilder group(
            @Nullable String group
    ) {
        this.group = group;
        return this;
    }

    public ShapedForgingRecipeBuilder tier(
            @Nullable AnvilTier tier
    ) {
        this.tier = tier == null
                ? null
                : tier.getDisplayName();

        return this;
    }

    public ShapedForgingRecipeBuilder setQuality(
            @Nullable boolean hasQuality
    ) {
        this.hasQuality = hasQuality;
        return this;
    }

    public ShapedForgingRecipeBuilder requiresBlueprint(
            @Nullable boolean requiresBlueprint
    ) {
        this.requiresBlueprint = requiresBlueprint;
        return this;
    }

    public ShapedForgingRecipeBuilder needsMinigame(
            @Nullable boolean needsMinigame
    ) {
        this.needsMinigame = needsMinigame;
        return this;
    }

    public ShapedForgingRecipeBuilder failedResult(
            ItemConvertible result
    ) {
        this.failedResult = result.asItem();
        this.failedResultCount = 1;
        return this;
    }

    public ShapedForgingRecipeBuilder failedResult(
            ItemConvertible result,
            int count
    ) {
        this.failedResult = result.asItem();
        this.failedResultCount = count;
        return this;
    }

    public ShapedForgingRecipeBuilder setBlueprint(
            String blueprintType
    ) {
        if (blueprintType != null && !blueprintType.isBlank()) {
            blueprintTypes.add(
                    blueprintType.toLowerCase(java.util.Locale.ROOT)
            );
        }

        return this;
    }

    public ShapedForgingRecipeBuilder minimumQuality(
            @Nullable ForgingQuality minimumQuality
    ) {
        this.minimumQuality = minimumQuality;
        return this;
    }

    public ShapedForgingRecipeBuilder qualityDifficulty(
            @Nullable ForgingQuality qualityDifficulty
    ) {
        this.qualityDifficulty = qualityDifficulty;
        return this;
    }

    public ShapedForgingRecipeBuilder setPolishing(
            @Nullable boolean hasPolishing
    ) {
        this.hasPolishing = hasPolishing;
        return this;
    }

    public ShapedForgingRecipeBuilder showNotification(
            boolean showNotification
    ) {
        this.showNotification = showNotification;
        return this;
    }

    public ShapedForgingRecipeBuilder setNeedQuenching(
            @Nullable boolean needQuenching
    ) {
        this.needQuenching = needQuenching;
        return this;
    }

    public Item getOutputItem() {
        return result;
    }

    public Item getFailedResult() {
        return failedResult;
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter) {
        offerTo(exporter, getItemId(this.getOutputItem()));
    }

    public void offerTo(
            Consumer<RecipeJsonProvider> exporter,
            Identifier recipeId
    ) {
        validate(recipeId);

        advancement
                .parent(ROOT)
                .criterion(
                        "has_the_recipe",
                        RecipeUnlockedCriterion.create(recipeId)
                )
                .rewards(
                        AdvancementRewards.Builder.recipe(recipeId)
                )
                .criteriaMerger(CriterionMerger.OR);

        exporter.accept(
                new Result(
                        recipeId,
                        hammering,
                        new ItemStack(result, count),
                        failedResult != null
                                ? new ItemStack(
                                failedResult,
                                failedResultCount
                        )
                                : ItemStack.EMPTY,
                        group == null ? "" : group,
                        category,
                        rows,
                        key,
                        advancement,
                        recipeId.withPrefixedPath(
                                "recipes/"
                                        + category.getFolderName()
                                        + "/"
                        ),
                        showNotification,
                        blueprintTypes,

                        hasQuality != null && !hasQuality
                                ? null
                                : requiresBlueprint != null
                                ? requiresBlueprint
                                : false,

                        hasQuality == null || hasQuality,

                        hasQuality != null && !hasQuality
                                ? null
                                : hasPolishing != null
                                ? hasPolishing
                                : true,

                        hasQuality != null && hasQuality
                                ? null
                                : needsMinigame != null
                                && needsMinigame,

                        hasQuality != null && !hasQuality
                                ? ""
                                : minimumQuality != null
                                ? minimumQuality.getDisplayName()
                                : ForgingQuality.POOR.getDisplayName(),

                        qualityDifficulty != null
                                ? qualityDifficulty.getDisplayName()
                                : ForgingQuality.NONE.getDisplayName(),

                        tier == null ? "" : tier,

                        needQuenching == null || needQuenching
                )
        );
    }

    private void validate(Identifier recipeId) {
        if (rows.isEmpty()) {
            throw new IllegalStateException(
                    "No pattern is defined for shaped forging recipe "
                            + recipeId + "!"
            );
        }

        int width = rows.get(0).length();

        for (String row : rows) {
            if (row.length() != width) {
                throw new IllegalStateException(
                        "Pattern must be the same width on every line!"
                );
            }
        }
    }

    static class Result implements RecipeJsonProvider {

        private final Identifier id;
        private final int hammering;
        private final ItemStack result;
        private final ItemStack failedResult;

        private final List<String> pattern;
        private final Map<Character, Ingredient> key;

        private final Advancement.Builder advancement;
        private final Identifier advancementId;

        private final boolean showNotification;
        private final String group;

        private final List<String> blueprintTypes;
        private final ForgingBookCategory category;

        private final Boolean requiresBlueprint;
        private final Boolean hasQuality;
        private final Boolean hasPolishing;
        private final Boolean needsMinigame;

        private final String minimumQuality;
        private final String qualityDifficulty;

        private final String tier;
        private final Boolean needQuenching;

        public Result(
                Identifier id,
                int hammering,
                ItemStack result,
                ItemStack failedResult,
                String group,
                ForgingBookCategory category,
                List<String> pattern,
                Map<Character, Ingredient> key,
                Advancement.Builder advancement,
                Identifier advancementId,
                boolean showNotification,
                List<String> blueprintTypes,
                Boolean requiresBlueprint,
                Boolean hasQuality,
                Boolean hasPolishing,
                Boolean needsMinigame,
                String minimumQuality,
                String qualityDifficulty,
                String tier,
                Boolean needQuenching
        ) {
            this.id = id;
            this.hammering = hammering;
            this.result = result;
            this.failedResult = failedResult;
            this.group = group;
            this.category = category;
            this.pattern = pattern;
            this.key = key;
            this.advancement = advancement;
            this.advancementId = advancementId;
            this.showNotification = showNotification;
            this.blueprintTypes = blueprintTypes;
            this.requiresBlueprint = requiresBlueprint;
            this.hasQuality = hasQuality;
            this.hasPolishing = hasPolishing;
            this.needsMinigame = needsMinigame;
            this.minimumQuality = minimumQuality;
            this.qualityDifficulty = qualityDifficulty;
            this.tier = tier;
            this.needQuenching = needQuenching;
        }

        @Override
        public void serialize(JsonObject json) {

            if (!group.isEmpty()) {
                json.addProperty("group", group);
            }

            if (requiresBlueprint != null) {
                json.addProperty(
                        "requires_blueprint",
                        requiresBlueprint
                );
            }

            if (!blueprintTypes.isEmpty()) {
                JsonArray blueprintArray = new JsonArray();

                for (String type : blueprintTypes) {
                    blueprintArray.add(type);
                }

                json.add("blueprint", blueprintArray);
            }

            if (category != null) {
                json.addProperty(
                        "category",
                        category.name()
                                .toLowerCase(
                                        java.util.Locale.ROOT
                                )
                );
            }

            if (!tier.isBlank()) {
                json.addProperty("tier", tier);
            }

            json.addProperty("hammering", hammering);

            if (hasQuality != null) {
                json.addProperty(
                        "has_quality",
                        hasQuality
                );
            }

            if (!minimumQuality.isEmpty()) {
                json.addProperty(
                        "minimum_quality",
                        minimumQuality
                );
            }

            if (!qualityDifficulty.isEmpty()) {
                json.addProperty(
                        "quality_difficulty",
                        qualityDifficulty
                );
            }

            if (needsMinigame != null) {
                json.addProperty(
                        "needs_minigame",
                        needsMinigame
                );
            }

            if (needQuenching != null) {
                json.addProperty(
                        "need_quenching",
                        needQuenching
                );
            }

            if (hasPolishing != null) {
                json.addProperty(
                        "has_polishing",
                        hasPolishing
                );
            }

            JsonArray patternArray = new JsonArray();

            for (String row : pattern) {
                patternArray.add(row);
            }

            json.add("pattern", patternArray);

            JsonObject keyObject = new JsonObject();

            for (Map.Entry<Character, Ingredient> entry : key.entrySet()) {
                keyObject.add(
                        String.valueOf(entry.getKey()),
                        entry.getValue().toJson()
                );
            }

            json.add("key", keyObject);

            JsonObject resultObject = new JsonObject();

            resultObject.addProperty(
                    "item",
                    Registries.ITEM
                            .getId(result.getItem())
                            .toString()
            );

            if (result.getCount() > 1) {
                resultObject.addProperty(
                        "count",
                        result.getCount()
                );
            }

            json.add("result", resultObject);

            if (!failedResult.isEmpty()) {
                JsonObject failedResultObject = new JsonObject();

                failedResultObject.addProperty(
                        "item",
                        Registries.ITEM
                                .getId(failedResult.getItem())
                                .toString()
                );

                if (failedResult.getCount() > 1) {
                    failedResultObject.addProperty(
                            "count",
                            failedResult.getCount()
                    );
                }

                json.add(
                        "result_failed",
                        failedResultObject
                );
            }

            json.addProperty(
                    "show_notification",
                    showNotification
            );
        }

        @Override
        public Identifier getRecipeId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return ForgingRecipe.Serializer.INSTANCE;
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