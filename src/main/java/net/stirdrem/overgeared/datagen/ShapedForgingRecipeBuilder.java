package net.stirdrem.overgeared.datagen;

import com.google.common.collect.Lists;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.recipe.ForgingBookCategory;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/** Datagen builder for {@code overgeared:forging} recipes. */
public class ShapedForgingRecipeBuilder {

    private final ForgingBookCategory category;
    private final Item result;
    private final int count;
    private final int hammering;

    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Function<HolderGetter<Item>, Ingredient>> key = new LinkedHashMap<>();
    private final RecipeBuilderSupport.Unlocks unlocks = new RecipeBuilderSupport.Unlocks();

    private final Set<String> blueprintTypes = new LinkedHashSet<>();

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

    public ShapedForgingRecipeBuilder(ForgingBookCategory category, ItemLike result, int count, int hammering) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;
        this.hammering = hammering;
    }

    public static boolean isToolPart(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModTags.Items.TOOL_PARTS);
    }

    public static boolean isToolPart(Item item) {
        return item.getDefaultInstance().is(ModTags.Items.TOOL_PARTS);
    }

    public static ShapedForgingRecipeBuilder create(ForgingBookCategory category, ItemLike result, int hammering) {
        return new ShapedForgingRecipeBuilder(category, result, 1, hammering);
    }

    public static ShapedForgingRecipeBuilder create(ForgingBookCategory category, ItemLike result, int count, int hammering) {
        return new ShapedForgingRecipeBuilder(category, result, count, hammering);
    }

    public ShapedForgingRecipeBuilder input(Character symbol, TagKey<Item> tag) {
        return input(symbol, RecipeBuilderSupport.lazy(tag));
    }

    public ShapedForgingRecipeBuilder input(Character symbol, ItemLike item) {
        return input(symbol, Ingredient.of(item));
    }

    public ShapedForgingRecipeBuilder input(Character symbol, Ingredient ingredient) {
        return input(symbol, RecipeBuilderSupport.lazy(ingredient));
    }

    private ShapedForgingRecipeBuilder input(Character symbol, Function<HolderGetter<Item>, Ingredient> ingredient) {
        if (key.containsKey(symbol)) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
        }
        if (symbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        }
        key.put(symbol, ingredient);
        return this;
    }

    public ShapedForgingRecipeBuilder pattern(String pattern) {
        if (!rows.isEmpty() && pattern.length() != rows.getFirst().length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        }
        rows.add(pattern);
        return this;
    }

    public ShapedForgingRecipeBuilder criterion(String name, Criterion<?> conditions) {
        unlocks.add(name, conditions);
        return this;
    }

    public ShapedForgingRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public ShapedForgingRecipeBuilder tier(@Nullable AnvilTier tier) {
        this.tier = tier == null ? null : tier.getDisplayName();
        return this;
    }

    public ShapedForgingRecipeBuilder setQuality(boolean hasQuality) {
        this.hasQuality = hasQuality;
        return this;
    }

    public ShapedForgingRecipeBuilder requiresBlueprint(boolean requiresBlueprint) {
        this.requiresBlueprint = requiresBlueprint;
        return this;
    }

    public ShapedForgingRecipeBuilder needsMinigame(boolean needsMinigame) {
        this.needsMinigame = needsMinigame;
        return this;
    }

    public ShapedForgingRecipeBuilder failedResult(ItemLike result) {
        return failedResult(result, 1);
    }

    public ShapedForgingRecipeBuilder failedResult(ItemLike result, int count) {
        this.failedResult = result.asItem();
        this.failedResultCount = count;
        return this;
    }

    public ShapedForgingRecipeBuilder setBlueprint(String blueprintType) {
        if (blueprintType != null && !blueprintType.isBlank()) {
            blueprintTypes.add(blueprintType.toLowerCase(Locale.ROOT));
        }
        return this;
    }

    public ShapedForgingRecipeBuilder minimumQuality(@Nullable ForgingQuality minimumQuality) {
        this.minimumQuality = minimumQuality;
        return this;
    }

    public ShapedForgingRecipeBuilder qualityDifficulty(@Nullable ForgingQuality qualityDifficulty) {
        this.qualityDifficulty = qualityDifficulty;
        return this;
    }

    public ShapedForgingRecipeBuilder setPolishing(boolean hasPolishing) {
        this.hasPolishing = hasPolishing;
        return this;
    }

    public ShapedForgingRecipeBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    public ShapedForgingRecipeBuilder setNeedQuenching(boolean needQuenching) {
        this.needQuenching = needQuenching;
        return this;
    }

    public Item getOutputItem() {
        return result;
    }

    public Item getFailedResult() {
        return failedResult;
    }

    public void offerTo(RecipeOutput output) {
        offerTo(output, RecipeBuilderSupport.defaultId(result));
    }

    public void offerTo(RecipeOutput output, Identifier recipeId) {
        if (rows.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped forging recipe " + recipeId + "!");
        }

        HolderGetter<Item> items = RecipeBuilderSupport.items(output);
        Map<Character, ForgingRecipe.ForgingIngredient> resolvedKey = new LinkedHashMap<>();
        key.forEach((symbol, ingredient) -> resolvedKey.put(symbol, ForgingRecipe.ForgingIngredient.of(ingredient.apply(items))));

        // Same effective values the 1.20.1 builder wrote to JSON.
        boolean noQuality = hasQuality != null && !hasQuality;
        boolean outRequiresBlueprint = !noQuality && requiresBlueprint != null && requiresBlueprint;
        boolean outHasQuality = hasQuality == null || hasQuality;
        boolean outHasPolishing = noQuality || hasPolishing == null || hasPolishing;
        boolean outNeedsMinigame = !(hasQuality != null && hasQuality) && needsMinigame != null && needsMinigame;
        ForgingQuality outMinimumQuality = noQuality || minimumQuality == null ? ForgingQuality.POOR : minimumQuality;
        ForgingQuality outQualityDifficulty = qualityDifficulty != null ? qualityDifficulty : ForgingQuality.NONE;
        boolean outNeedQuenching = needQuenching == null || needQuenching;

        ForgingRecipe recipe = new ForgingRecipe(
                group == null ? "" : group,
                category,
                outRequiresBlueprint,
                blueprintTypes,
                tier == null ? AnvilTier.IRON.getDisplayName() : tier,
                List.copyOf(rows),
                resolvedKey,
                new ItemStackTemplate(result, count),
                failedResult != null ? Optional.of(new ItemStackTemplate(failedResult, failedResultCount)) : Optional.empty(),
                hammering,
                outHasQuality,
                outNeedsMinigame,
                outHasPolishing,
                Optional.of(outNeedQuenching),
                showNotification,
                outMinimumQuality,
                outQualityDifficulty);

        ResourceKey<Recipe<?>> id = RecipeBuilderSupport.key(recipeId);
        output.accept(id, recipe, unlocks.build(output, id, category.getFolderName()));
    }
}
