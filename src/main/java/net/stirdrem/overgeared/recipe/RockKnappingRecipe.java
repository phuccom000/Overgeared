package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Rock knapping. Input: {@link ItemListInput} of exactly 9 stacks (3x3 grid, row-major); an empty slot is a
 * chipped spot, a non-empty slot must match {@link #getIngredient()}.
 * <p>
 * JSON: {@code ingredient}, {@code result}, {@code pattern} (rows of 'x'/'X' = chipped, anything else = kept),
 * optional {@code mirrored}.
 */
public class RockKnappingRecipe implements Recipe<ItemListInput> {

    private final ItemStackTemplate output;
    private final Ingredient ingredient;
    private final List<String> patternRows;
    private final boolean mirrored;

    private final boolean[][] pattern;
    private final int width;
    private final int height;

    public RockKnappingRecipe(ItemStackTemplate output, Ingredient ingredient, List<String> patternRows, boolean mirrored) {
        this.output = output;
        this.ingredient = ingredient;
        this.patternRows = List.copyOf(patternRows);
        this.mirrored = mirrored;
        this.height = patternRows.size();
        this.width = patternRows.getFirst().length();
        this.pattern = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            String row = patternRows.get(y);
            for (int x = 0; x < width; x++) {
                char c = row.charAt(x);
                pattern[y][x] = (c == 'x' || c == 'X');
            }
        }
    }

    /* ---------------- MATCHING LOGIC ---------------- */

    @Override
    public boolean matches(ItemListInput inv, Level world) {
        if (inv.size() != 9) return false;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && !ingredient.test(stack)) {
                return false;
            }
        }

        boolean[][] input = new boolean[3][3];
        for (int i = 0; i < 9; i++) {
            input[i / 3][i % 3] = inv.getItem(i).isEmpty(); // true = chipped
        }

        for (int y = 0; y <= 3 - height; y++) {
            for (int x = 0; x <= 3 - width; x++) {
                if (matchesAt(input, x, y, false)) return true;
                if (mirrored && matchesAt(input, x, y, true)) return true;
            }
        }

        return false;
    }

    private boolean matchesAt(boolean[][] input, int ox, int oy, boolean mirror) {
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int sx = mirror ? width - 1 - px : px;
                if (pattern[py][sx] != input[oy + py][ox + px]) {
                    return false;
                }
            }
        }

        // Outside pattern must be chipped
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                boolean inside = x >= ox && x < ox + width && y >= oy && y < oy + height;
                if (!inside && input[y][x]) {
                    return false;
                }
            }
        }

        return true;
    }

    /* ---------------- RECIPE OUTPUT ---------------- */

    @Override
    public ItemStack assemble(ItemListInput inv) {
        return output.create();
    }

    /** A fresh copy of the result. */
    public ItemStack getResultItem() {
        return output.create();
    }

    /** @deprecated use {@link #getResultItem()}. */
    @Deprecated
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getResultItem();
    }

    public ItemStackTemplate result() {
        return output;
    }

    /* ---------------- GETTERS ---------------- */

    /** [row][column]; true = chipped (empty) spot. */
    public boolean[][] getPattern() {
        return pattern;
    }

    public List<String> getPatternRows() {
        return patternRows;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isMirrored() {
        return mirrored;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    /* ---------------- RECIPE META ---------------- */

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<RockKnappingRecipe> getSerializer() {
        return ModRecipes.ROCK_KNAPPING_SERIALIZER;
    }

    @Override
    public RecipeType<RockKnappingRecipe> getType() {
        return ModRecipeTypes.KNAPPING;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredient);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.KNAPPING;
    }

    /* ---------------- TYPE ---------------- */

    public static class Type implements RecipeType<RockKnappingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "rock_knapping";

        @Override
        public String toString() {
            return ID;
        }
    }

    /* ---------------- SERIALIZER ---------------- */

    public static final MapCodec<RockKnappingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RecipeCodecs.RESULT.fieldOf("result").forGetter(r -> r.output),
            RecipeCodecs.INGREDIENT.fieldOf("ingredient").forGetter(r -> r.ingredient),
            RecipeCodecs.pattern(3).fieldOf("pattern").forGetter(r -> r.patternRows),
            Codec.BOOL.optionalFieldOf("mirrored", false).forGetter(r -> r.mirrored)
    ).apply(i, RockKnappingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RockKnappingRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC, r -> r.output,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), r -> r.patternRows,
            ByteBufCodecs.BOOL, r -> r.mirrored,
            RockKnappingRecipe::new);

    public static final RecipeSerializer<RockKnappingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
