package net.stirdrem.overgeared.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;

import java.util.List;
import java.util.Optional;

/**
 * {@code overgeared:crafting_shapeless}: a vanilla shapeless crafting recipe (type {@code minecraft:crafting},
 * input {@link CraftingInput}) that
 * <ul>
 *     <li>passes the forging quality / creator of its ingredients on to the result (downgraded when an
 *     ingredient is unpolished or still heated; blocked entirely when the minigame is disabled), and</li>
 *     <li>supports ingredients that stay in the grid ({@code remainder}), optionally losing durability.</li>
 * </ul>
 * JSON: vanilla shapeless fields; each {@code ingredients} entry is a plain ingredient or
 * {@code {"ingredient": <ingredient>, "remainder": bool, "durability_decrease": int}} (the 1.20.1 form
 * {@code {"item": ..., "remainder": ...}} is still accepted).
 */
public class OvergearedShapelessRecipe extends ShapelessRecipe {

    private final Recipe.CommonInfo info;
    private final CraftingRecipe.CraftingBookInfo book;
    private final ItemStackTemplate resultTemplate;
    private final List<IngredientWithRemainder> ingredientsWithRemainder;

    public OvergearedShapelessRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                     ItemStackTemplate result, List<IngredientWithRemainder> ingredientsWithRemainder) {
        super(commonInfo, bookInfo, result, ingredientsWithRemainder.stream().map(IngredientWithRemainder::getIngredient).toList());
        this.info = commonInfo;
        this.book = bookInfo;
        this.resultTemplate = result;
        this.ingredientsWithRemainder = List.copyOf(ingredientsWithRemainder);
    }

    public List<IngredientWithRemainder> getIngredientsWithRemainder() {
        return ingredientsWithRemainder;
    }

    public List<Ingredient> getIngredients() {
        return ingredientsWithRemainder.stream().map(IngredientWithRemainder::getIngredient).toList();
    }

    /** A fresh copy of the plain result (no quality applied). */
    public ItemStack getResultItem() {
        return resultTemplate.create();
    }

    public ItemStackTemplate result() {
        return resultTemplate;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(container.size(), ItemStack.EMPTY);
        boolean[] ingredientProcessed = new boolean[ingredientsWithRemainder.size()];

        for (int slot = 0; slot < container.size(); slot++) {
            ItemStack slotStack = container.getItem(slot);
            if (slotStack.isEmpty()) continue;

            for (int ingIndex = 0; ingIndex < ingredientsWithRemainder.size(); ingIndex++) {
                IngredientWithRemainder ingredient = ingredientsWithRemainder.get(ingIndex);
                if (!ingredientProcessed[ingIndex] && ingredient.getIngredient().test(slotStack)) {
                    if (ingredient.hasRemainder()) {
                        ItemStack remainder = ingredient.getRemainder(slotStack);
                        if (!remainder.isEmpty()) {
                            remainingItems.set(slot, remainder);
                        }
                    }
                    ingredientProcessed[ingIndex] = true;
                    break;
                }
            }
        }

        return remainingItems;
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        ItemStack result = resultTemplate.create();

        boolean unpolished = false;
        boolean unquenched = false;
        ForgingQuality foundQuality = null;
        String creator = null;
        for (int i = 0; i < container.size(); i++) {
            ItemStack ingredient = container.getItem(i);
            if (ingredient.isEmpty()) continue;
            if (Boolean.FALSE.equals(ingredient.get(ModComponents.POLISHED))) unpolished = true;
            if (Boolean.TRUE.equals(ingredient.get(ModComponents.HEATED))) unquenched = true;
            ForgingQuality q = ingredient.get(ModComponents.FORGING_QUALITY);
            if (q != null && q != ForgingQuality.NONE) foundQuality = q;
            String c = ingredient.get(ModComponents.CREATOR);
            if (c != null) creator = c;
        }

        if (!ServerConfig.ENABLE_MINIGAME.get()) {
            // Prevent crafting if any unpolished / unquenched quality items exist
            if (unpolished || unquenched) {
                return ItemStack.EMPTY;
            }
            result.set(ModComponents.FORGING_QUALITY, foundQuality != null ? foundQuality : ForgingQuality.POOR);
            if (creator != null) result.set(ModComponents.CREATOR, creator);
            return result;
        }

        if (foundQuality == null) {
            if (unpolished || unquenched) {
                result.set(ModComponents.FORGING_QUALITY, ForgingQuality.POOR);
            }
            return result;
        }

        ForgingQuality quality = foundQuality;
        if (unpolished) quality = quality.getLowerQuality();
        if (unquenched) quality = quality.getLowerQuality();
        result.set(ModComponents.FORGING_QUALITY, quality);
        if (creator != null) result.set(ModComponents.CREATOR, creator);
        return result;
    }

    @Override
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        RecipeSerializer<ShapelessRecipe> s = (RecipeSerializer) ModRecipes.CRAFTING_SHAPELESS;
        return s;
    }

    /** @deprecated recipes of this serializer have type {@code minecraft:crafting}; kept for source compatibility. */
    @Deprecated
    public static class Type implements RecipeType<OvergearedShapelessRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "crafting_shapeless";
    }

    // ------------------------------------------------------------------------------------------
    // ingredient with remainder
    // ------------------------------------------------------------------------------------------

    public static class IngredientWithRemainder {
        private final Ingredient ingredient;
        private final boolean remainder;
        private final int durabilityDecrease;

        public IngredientWithRemainder(Ingredient ingredient, boolean remainder, int durabilityDecrease) {
            this.ingredient = ingredient;
            this.remainder = remainder;
            this.durabilityDecrease = durabilityDecrease;
        }

        public static IngredientWithRemainder of(Ingredient ingredient) {
            return new IngredientWithRemainder(ingredient, false, 0);
        }

        public Ingredient getIngredient() {
            return ingredient;
        }

        public boolean hasRemainder() {
            return remainder;
        }

        public int getDurabilityDecrease() {
            return durabilityDecrease;
        }

        public ItemStack getRemainder(ItemStack original) {
            if (!remainder) {
                return ItemStack.EMPTY;
            }

            ItemStack remainderStack = original.copyWithCount(1);

            if (durabilityDecrease > 0 && remainderStack.isDamageableItem()) {
                int newDamage = remainderStack.getDamageValue() + durabilityDecrease;
                if (newDamage >= remainderStack.getMaxDamage()) {
                    return ItemStack.EMPTY; // Item breaks
                }
                remainderStack.setDamageValue(newDamage);
            }

            return remainderStack;
        }

        private static final MapCodec<IngredientWithRemainder> OBJECT_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                RecipeCodecs.INGREDIENT.fieldOf("ingredient").forGetter(IngredientWithRemainder::getIngredient),
                Codec.BOOL.optionalFieldOf("remainder", false).forGetter(IngredientWithRemainder::hasRemainder),
                Codec.INT.optionalFieldOf("durability_decrease", 0).forGetter(IngredientWithRemainder::getDurabilityDecrease)
        ).apply(i, IngredientWithRemainder::new));

        public static final Codec<IngredientWithRemainder> CODEC = new Codec<>() {
            @Override
            public <T> DataResult<Pair<IngredientWithRemainder, T>> decode(DynamicOps<T> ops, T input) {
                Optional<MapLike<T>> map = ops.getMap(input).result();
                if (map.isPresent() && map.get().get("ingredient") != null) {
                    return OBJECT_CODEC.decoder().decode(ops, input);
                }
                return RecipeCodecs.INGREDIENT.decode(ops, input).map(pair -> {
                    boolean remainder = map.map(m -> m.get("remainder")).map(v -> ops.getBooleanValue(v).result().orElse(false)).orElse(false);
                    int decrease = map.map(m -> m.get("durability_decrease")).map(v -> ops.getNumberValue(v).result().map(Number::intValue).orElse(0)).orElse(0);
                    return Pair.of(new IngredientWithRemainder(pair.getFirst(), remainder, decrease), input);
                });
            }

            @Override
            public <T> DataResult<T> encode(IngredientWithRemainder input, DynamicOps<T> ops, T prefix) {
                if (!input.remainder && input.durabilityDecrease == 0) {
                    return RecipeCodecs.INGREDIENT.encode(input.ingredient, ops, prefix);
                }
                return OBJECT_CODEC.codec().encode(input, ops, prefix);
            }
        };

        public static final StreamCodec<RegistryFriendlyByteBuf, IngredientWithRemainder> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, IngredientWithRemainder::getIngredient,
                ByteBufCodecs.BOOL, IngredientWithRemainder::hasRemainder,
                ByteBufCodecs.VAR_INT, IngredientWithRemainder::getDurabilityDecrease,
                IngredientWithRemainder::new);
    }

    // ------------------------------------------------------------------------------------------
    // serialization
    // ------------------------------------------------------------------------------------------

    public static final MapCodec<OvergearedShapelessRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.info),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.book),
            RecipeCodecs.RESULT.fieldOf("result").forGetter(o -> o.resultTemplate),
            IngredientWithRemainder.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(o -> o.ingredientsWithRemainder)
    ).apply(i, OvergearedShapelessRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, OvergearedShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.info,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.book,
            ItemStackTemplate.STREAM_CODEC, o -> o.resultTemplate,
            IngredientWithRemainder.STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.ingredientsWithRemainder,
            OvergearedShapelessRecipe::new);

    public static final RecipeSerializer<OvergearedShapelessRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
