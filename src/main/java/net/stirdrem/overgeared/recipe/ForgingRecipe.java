package net.stirdrem.overgeared.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Smithing anvil recipe. Input: {@link ItemListInput} of the anvil container - slots 0..8 are the 3x3
 * grid (row-major), slot 11 is the blueprint slot (slots 9/10 hammer/output are ignored).
 */
public class ForgingRecipe implements Recipe<ItemListInput> {
    public static final int BLUEPRINT_SLOT = 11;

    private final Core core;
    private final Settings settings;

    // derived from the pattern
    public final int width;
    public final int height;
    private final List<ForgingIngredient> ingredients;
    private PlacementInfo placementInfo;

    public ForgingRecipe(Core core, Settings settings) {
        this.core = core;
        this.settings = settings;
        this.height = core.pattern().size();
        this.width = core.pattern().getFirst().length();
        List<ForgingIngredient> list = new ArrayList<>(width * height);
        for (String row : core.pattern()) {
            for (int x = 0; x < width; x++) {
                char c = row.charAt(x);
                ForgingIngredient ing = core.key().get(c);
                if (ing == null) {
                    if (c != ' ') throw new IllegalArgumentException("Pattern references undefined symbol: '" + c + "'");
                    ing = ForgingIngredient.EMPTY;
                }
                list.add(ing);
            }
        }
        this.ingredients = List.copyOf(list);
    }

    /** Convenience constructor used by datagen. */
    public ForgingRecipe(String group, ForgingBookCategory category, boolean requiresBlueprint, Set<String> blueprintTypes, String tier,
                         List<String> pattern, Map<Character, ForgingIngredient> key, ItemStackTemplate result,
                         Optional<ItemStackTemplate> failedResult, int hammering, boolean hasQuality, boolean needsMinigame,
                         boolean hasPolishing, Optional<Boolean> needQuenching, boolean showNotification,
                         ForgingQuality minimumQuality, ForgingQuality qualityDifficulty) {
        this(new Core(group, category, requiresBlueprint, blueprintTypes, tier, pattern, key, result, failedResult),
                new Settings(hammering, hasQuality, needsMinigame, hasPolishing, needQuenching, showNotification, minimumQuality, qualityDifficulty));
    }

    // ------------------------------------------------------------------------------------------
    // lookup helpers
    // ------------------------------------------------------------------------------------------

    public static Optional<ForgingRecipe> findBestMatch(Level world, Container inv) {
        return findBestMatch(world, ItemListInput.of(inv));
    }

    /** Best (largest pattern) forging recipe matching the anvil contents; works on both sides. */
    public static Optional<ForgingRecipe> findBestMatch(Level world, ItemListInput inv) {
        ItemStack keyStack = IntStream.range(0, 9).mapToObj(inv::getItem).filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
        if (keyStack.isEmpty()) {
            return Optional.empty();
        }

        return RecipeLookup.<ItemListInput, ForgingRecipe>allValues(world, ModRecipeTypes.FORGING)
                .stream()
                .filter(recipe -> recipe.containsIngredient(keyStack))
                .filter(recipe -> recipe.matches(inv, world))
                .max(Comparator.comparingInt(ForgingRecipe::getRecipeSize));
    }

    public boolean containsIngredient(ItemStack stack) {
        for (ForgingIngredient ing : this.ingredients) {
            if (!ing.isEmpty() && ing.test(stack)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------
    // matching
    // ------------------------------------------------------------------------------------------

    private boolean checkBlueprint(ItemListInput inv) {
        ItemStack blueprintStack = inv.getItem(BLUEPRINT_SLOT);

        if (!core.requiresBlueprint() && core.blueprintTypes().isEmpty()) {
            return blueprintStack.isEmpty();
        }

        if (blueprintStack.isEmpty()) {
            return !core.requiresBlueprint();
        }

        BlueprintData data = blueprintStack.get(ModComponents.BLUEPRINT_DATA);
        if (data == null) return false;

        return core.blueprintTypes().contains(data.toolType());
    }

    @Override
    public boolean matches(ItemListInput inv, Level world) {
        if (!checkBlueprint(inv)) return false;

        for (int y = 0; y <= 3 - height; y++) {
            for (int x = 0; x <= 3 - width; x++) {
                if (matchesPattern(inv, x, y) && checkSurroundingBlanks(inv, x, y)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkSurroundingBlanks(ItemListInput inv, int xOffset, int yOffset) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (x >= xOffset && x < xOffset + width && y >= yOffset && y < yOffset + height) {
                    continue;
                }
                if (!inv.getItem(y * 3 + x).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean matchesPattern(ItemListInput inv, int xOffset, int yOffset) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ItemStack stack = inv.getItem((y + yOffset) * 3 + (x + xOffset));
                ForgingIngredient ingredient = ingredients.get(y * width + x);
                if (ingredient.isEmpty()) {
                    if (!stack.isEmpty()) return false;
                } else if (!ingredient.test(stack)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Result with the components of every {@code transfer_nbt} ingredient applied (ingredient index i is read
     * from input slot i, as in 1.20.1). The anvil block entity builds its own output; this is for generic callers.
     */
    @Override
    public ItemStack assemble(ItemListInput inv) {
        ItemStack out = core.result().create();
        for (int i = 0; i < ingredients.size(); i++) {
            if (!ingredients.get(i).transferNbt()) continue;
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            out.applyComponents(stack.getComponentsPatch());
        }
        return out;
    }

    // ------------------------------------------------------------------------------------------
    // results
    // ------------------------------------------------------------------------------------------

    /** A fresh copy of the result stack. */
    public ItemStack getResultItem() {
        return core.result().create();
    }

    /** @deprecated registries are no longer needed; use {@link #getResultItem()}. */
    @Deprecated
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getResultItem();
    }

    public ItemStackTemplate result() {
        return core.result();
    }

    public boolean hasFailedResult() {
        return core.failedResult().isPresent() && core.failedResult().get().item() != core.result().item();
    }

    /** Fresh copy of the failed result, or EMPTY if this recipe has none. */
    public ItemStack getFailedResultItem() {
        return hasFailedResult() ? core.failedResult().get().create() : ItemStack.EMPTY;
    }

    /** @deprecated use {@link #getFailedResultItem()}. */
    @Deprecated
    public ItemStack getFailedResultItem(HolderLookup.Provider registries) {
        return getFailedResultItem();
    }

    public Optional<ItemStackTemplate> failedResult() {
        return core.failedResult();
    }

    // ------------------------------------------------------------------------------------------
    // ingredients
    // ------------------------------------------------------------------------------------------

    /** Pattern ingredients, row-major, {@code width * height} entries; blanks are empty. */
    public List<Optional<Ingredient>> getIngredients() {
        return ingredients.stream().map(ForgingIngredient::ingredient).toList();
    }

    /** Pattern ingredients, row-major, {@code width * height} entries; blanks are {@link ForgingIngredient#EMPTY}. */
    public List<ForgingIngredient> getForgingIngredients() {
        return ingredients;
    }

    public List<String> getPattern() {
        return core.pattern();
    }

    public Map<Character, ForgingIngredient> getKey() {
        return core.key();
    }

    // ------------------------------------------------------------------------------------------
    // Recipe
    // ------------------------------------------------------------------------------------------

    @Override
    public RecipeSerializer<ForgingRecipe> getSerializer() {
        return ModRecipes.FORGING_SERIALIZER;
    }

    @Override
    public RecipeType<ForgingRecipe> getType() {
        return ModRecipeTypes.FORGING;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (placementInfo == null) {
            placementInfo = PlacementInfo.createFromOptionals(getIngredients());
        }
        return placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.FORGING;
    }

    @Override
    public String group() {
        return core.group();
    }

    /** @deprecated 1.20.1 name; use {@link #group()}. */
    @Deprecated
    public String getGroup() {
        return group();
    }

    @Override
    public boolean showNotification() {
        return settings.showNotification();
    }

    // ------------------------------------------------------------------------------------------
    // forging properties
    // ------------------------------------------------------------------------------------------

    public int getHammeringRequired() {
        return settings.hammering();
    }

    public int getRemainingHits() {
        return settings.hammering();
    }

    public String getAnvilTier() {
        return core.tier();
    }

    public boolean hasQuality() {
        return settings.hasQuality();
    }

    public boolean needsMinigame() {
        return settings.needsMinigame();
    }

    public ForgingQuality getMinimumQuality() {
        return settings.minimumQuality();
    }

    public ForgingQuality getQualityDifficulty() {
        return settings.qualityDifficulty();
    }

    public boolean hasPolishing() {
        return settings.hasPolishing();
    }

    /** Defaults to "result is not armor" when the JSON doesn't say. */
    public boolean needQuenching() {
        return settings.needQuenching().orElseGet(() -> {
            Equippable equippable = core.result().get(DataComponents.EQUIPPABLE);
            return equippable == null || !equippable.slot().isArmor();
        });
    }

    public Set<String> getBlueprintTypes() {
        return core.blueprintTypes().stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
    }

    public boolean requiresBlueprint() {
        return core.requiresBlueprint();
    }

    public ForgingBookCategory getRecipeBookTab() {
        return core.category();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private int getRecipeSize() {
        return width * height;
    }

    // ------------------------------------------------------------------------------------------
    // type & serialization
    // ------------------------------------------------------------------------------------------

    public static class Type implements RecipeType<ForgingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "forging";

        @Override
        public String toString() {
            return ID;
        }
    }

    private static final Codec<Set<String>> BLUEPRINT_TYPES_CODEC = RecipeCodecs.singleOrList(Codec.STRING).xmap(
            list -> list.stream().map(s -> s.toLowerCase(Locale.ROOT)).filter(s -> !s.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new)),
            List::copyOf);

    private static final Codec<ForgingBookCategory> TAB_CODEC =
            Codec.STRING.xmap(ForgingBookCategory::findByName, ForgingBookCategory::getFolderName);

    /** "minimum_quality" (also reads the 1.20.1 key "minimumQuality"). */
    private static final MapCodec<ForgingQuality> MINIMUM_QUALITY_CODEC = Codec.mapPair(
            RecipeCodecs.FORGING_QUALITY.optionalFieldOf("minimum_quality"),
            RecipeCodecs.FORGING_QUALITY.optionalFieldOf("minimumQuality")
    ).xmap(
            pair -> pair.getFirst().or(pair::getSecond).orElse(ForgingQuality.POOR),
            quality -> Pair.of(quality == ForgingQuality.POOR ? Optional.empty() : Optional.of(quality), Optional.empty()));

    public record Core(String group, ForgingBookCategory category, boolean requiresBlueprint, Set<String> blueprintTypes,
                       String tier, List<String> pattern, Map<Character, ForgingIngredient> key,
                       ItemStackTemplate result, Optional<ItemStackTemplate> failedResult) {
        public static final MapCodec<Core> MAP_CODEC = RecordCodecBuilder.<Core>mapCodec(i -> i.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(Core::group),
                TAB_CODEC.optionalFieldOf("category", ForgingBookCategory.MISC).forGetter(Core::category),
                Codec.BOOL.optionalFieldOf("requires_blueprint", false).forGetter(Core::requiresBlueprint),
                BLUEPRINT_TYPES_CODEC.optionalFieldOf("blueprint", Set.of()).forGetter(Core::blueprintTypes),
                Codec.STRING.optionalFieldOf("tier", AnvilTier.IRON.getDisplayName()).forGetter(Core::tier),
                RecipeCodecs.pattern(3).fieldOf("pattern").forGetter(Core::pattern),
                RecipeCodecs.key(ForgingIngredient.CODEC).fieldOf("key").forGetter(Core::key),
                RecipeCodecs.RESULT.fieldOf("result").forGetter(Core::result),
                RecipeCodecs.RESULT.optionalFieldOf("result_failed").forGetter(Core::failedResult)
        ).apply(i, Core::new)).validate(Core::validate);

        private static DataResult<Core> validate(Core core) {
            for (String row : core.pattern()) {
                for (char c : row.toCharArray()) {
                    if (c != ' ' && !core.key().containsKey(c)) {
                        return DataResult.error(() -> "Pattern references undefined symbol: '" + c + "'");
                    }
                }
            }
            return DataResult.success(core);
        }

        public Core {
            blueprintTypes = java.util.Collections.unmodifiableSet(new LinkedHashSet<>(blueprintTypes));
            key = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(key));
        }
    }

    public record Settings(int hammering, boolean hasQuality, boolean needsMinigame, boolean hasPolishing,
                           Optional<Boolean> needQuenching, boolean showNotification,
                           ForgingQuality minimumQuality, ForgingQuality qualityDifficulty) {
        public static final MapCodec<Settings> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.INT.optionalFieldOf("hammering", 1).forGetter(Settings::hammering),
                Codec.BOOL.optionalFieldOf("has_quality", true).forGetter(Settings::hasQuality),
                Codec.BOOL.optionalFieldOf("needs_minigame", false).forGetter(Settings::needsMinigame),
                Codec.BOOL.optionalFieldOf("has_polishing", true).forGetter(Settings::hasPolishing),
                Codec.BOOL.optionalFieldOf("need_quenching").forGetter(Settings::needQuenching),
                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(Settings::showNotification),
                MINIMUM_QUALITY_CODEC.forGetter(Settings::minimumQuality),
                RecipeCodecs.FORGING_QUALITY.optionalFieldOf("quality_difficulty", ForgingQuality.NONE).forGetter(Settings::qualityDifficulty)
        ).apply(i, Settings::new));
    }

    public static final MapCodec<ForgingRecipe> MAP_CODEC = RecordCodecBuilder.<ForgingRecipe>mapCodec(i -> i.group(
            Core.MAP_CODEC.forGetter(r -> r.core),
            Settings.MAP_CODEC.forGetter(r -> r.settings)
    ).apply(i, ForgingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ForgingRecipe> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(MAP_CODEC.codec());

    public static final RecipeSerializer<ForgingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    // ------------------------------------------------------------------------------------------
    // ingredient
    // ------------------------------------------------------------------------------------------

    /**
     * One pattern slot. JSON: either a plain ingredient ({@code "minecraft:iron_ingot"}, {@code "#c:ingots"}, ...)
     * or {@code {"ingredient": <ingredient>, "requires_heated": bool, "transfer_nbt": bool}}. The 1.20.1 form
     * {@code {"item": ..., "requires_heated": ...}} is still accepted.
     *
     * @param ingredient    empty for a blank pattern slot (the grid slot must then be empty)
     * @param requiresHeated the stack must carry {@code overgeared:heated = true}
     * @param transferNbt   copy this stack's components onto the result
     */
    public record ForgingIngredient(Optional<Ingredient> ingredient, boolean requiresHeated, boolean transferNbt) {
        public static final ForgingIngredient EMPTY = new ForgingIngredient(Optional.empty(), false, false);

        public ForgingIngredient(Ingredient ingredient, boolean requiresHeated, boolean transferNbt) {
            this(Optional.of(ingredient), requiresHeated, transferNbt);
        }

        public static ForgingIngredient of(Ingredient ingredient) {
            return new ForgingIngredient(ingredient, false, false);
        }

        public boolean isEmpty() {
            return ingredient.isEmpty();
        }

        /** Tests a non-empty stack against this ingredient. Always false for {@link #EMPTY}. */
        public boolean test(ItemStack stack) {
            if (ingredient.isEmpty() || !ingredient.get().test(stack)) return false;
            return !requiresHeated || Boolean.TRUE.equals(stack.get(ModComponents.HEATED));
        }

        private static final MapCodec<ForgingIngredient> OBJECT_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                RecipeCodecs.INGREDIENT.fieldOf("ingredient").forGetter(f -> f.ingredient().orElseThrow()),
                Codec.BOOL.optionalFieldOf("requires_heated", false).forGetter(ForgingIngredient::requiresHeated),
                Codec.BOOL.optionalFieldOf("transfer_nbt", false).forGetter(ForgingIngredient::transferNbt)
        ).apply(i, ForgingIngredient::new));

        public static final Codec<ForgingIngredient> CODEC = new Codec<>() {
            @Override
            public <T> DataResult<Pair<ForgingIngredient, T>> decode(DynamicOps<T> ops, T input) {
                Optional<MapLike<T>> map = ops.getMap(input).result();
                if (map.isPresent() && map.get().get("ingredient") != null) {
                    return OBJECT_CODEC.decoder().decode(ops, input);
                }
                return RecipeCodecs.INGREDIENT.decode(ops, input).map(pair -> Pair.of(new ForgingIngredient(pair.getFirst(),
                        map.map(m -> getBool(ops, m, "requires_heated")).orElse(false),
                        map.map(m -> getBool(ops, m, "transfer_nbt")).orElse(false)), input));
            }

            private static <T> boolean getBool(DynamicOps<T> ops, MapLike<T> map, String key) {
                T value = map.get(key);
                return value != null && ops.getBooleanValue(value).result().orElse(false);
            }

            @Override
            public <T> DataResult<T> encode(ForgingIngredient input, DynamicOps<T> ops, T prefix) {
                if (input.isEmpty()) return DataResult.error(() -> "Cannot encode an empty forging ingredient");
                if (!input.requiresHeated() && !input.transferNbt()) {
                    return RecipeCodecs.INGREDIENT.encode(input.ingredient().get(), ops, prefix);
                }
                return OBJECT_CODEC.codec().encode(input, ops, prefix);
            }
        };
    }
}
