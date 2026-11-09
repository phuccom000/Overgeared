package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shared logic of the shaped alloy smelter recipes. Input: {@link ItemListInput} whose first
 * {@code gridSize * gridSize} slots are the input grid, row-major (2x2 alloy smelter, 3x3 nether alloy smelter).
 * <p>
 * JSON: {@code pattern} (trimmed like vanilla, must fit the grid), {@code key} (symbol -> ingredient; undefined
 * symbols and ' ' are blanks), {@code result}, optional {@code group}, {@code category}, {@code experience} (0),
 * {@code cookingtime} (200).
 */
public abstract class AbstractShapedAlloyRecipe implements Recipe<ItemListInput> {

    protected final String group;
    protected final CraftingBookCategory category;
    protected final List<String> rawPattern;
    protected final Map<Character, Ingredient> key;

    protected final int width;
    protected final int height;
    protected final int gridSize; // 2 or 3

    protected final List<Optional<Ingredient>> patterns;

    protected final ItemStackTemplate output;
    protected final float experience;
    protected final int cookingTime;
    private PlacementInfo placementInfo;

    protected AbstractShapedAlloyRecipe(int gridSize, String group, CraftingBookCategory category, List<String> rawPattern,
                                        Map<Character, Ingredient> key, ItemStackTemplate output, float experience, int cookingTime) {
        this.gridSize = gridSize;
        this.group = group;
        this.category = category;
        this.rawPattern = List.copyOf(rawPattern);
        this.key = Collections.unmodifiableMap(new LinkedHashMap<>(key));
        this.output = output;
        this.experience = experience;
        this.cookingTime = cookingTime;

        String[] trimmed = trimPattern(rawPattern);
        this.height = trimmed.length;
        this.width = height == 0 ? 0 : trimmed[0].length();
        List<Optional<Ingredient>> list = new ArrayList<>(width * height);
        for (String row : trimmed) {
            for (int x = 0; x < width; x++) {
                list.add(Optional.ofNullable(key.get(row.charAt(x))));
            }
        }
        this.patterns = List.copyOf(list);
    }

    // -----------------------
    // Matching logic
    // -----------------------

    @Override
    public boolean matches(ItemListInput inv, Level world) {
        if (world.isClientSide()) return false;

        for (int offY = 0; offY <= gridSize - height; offY++) {
            for (int offX = 0; offX <= gridSize - width; offX++) {
                if (matchesAt(inv, offX, offY)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean matchesAt(ItemListInput inv, int offX, int offY) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int recipeIndex = y * width + x;
                int invIndex = (y + offY) * gridSize + (x + offX);
                if (!Ingredient.testOptionalIngredient(patterns.get(recipeIndex), inv.getItem(invIndex))) {
                    return false;
                }
            }
        }

        for (int i = 0; i < gridSize * gridSize; i++) {
            int x = i % gridSize;
            int y = i / gridSize;
            boolean inside = x >= offX && x < offX + width && y >= offY && y < offY + height;
            if (!inside && !inv.getItem(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    // -----------------------
    // Boilerplate
    // -----------------------

    @Override
    public ItemStack assemble(ItemListInput inv) {
        return output.create();
    }

    public ItemStack getResultItem() {
        return output.create();
    }

    public ItemStackTemplate result() {
        return output;
    }

    @Override
    public String group() {
        return group;
    }

    /** @deprecated 1.20.1 name; use {@link #group()}. */
    @Deprecated
    public String getGroup() {
        return group;
    }

    public CraftingBookCategory category() {
        return category;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getGridSize() {
        return gridSize;
    }

    public boolean isShaped() {
        return true;
    }

    /** Trimmed pattern, row-major, {@code width * height} entries; blanks are empty. */
    public List<Optional<Ingredient>> getIngredientsList() {
        return patterns;
    }

    public float getExperience() {
        return experience;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (placementInfo == null) {
            placementInfo = PlacementInfo.createFromOptionals(patterns);
        }
        return placementInfo;
    }

    // -----------------------
    // Serialization helpers
    // -----------------------

    static String[] trimPattern(List<String> rows) {
        int minX = Integer.MAX_VALUE;
        int maxX = 0;
        int minY = 0;
        int maxY = rows.size();

        while (minY < maxY && rows.get(minY).trim().isEmpty()) minY++;
        while (maxY > minY && rows.get(maxY - 1).trim().isEmpty()) maxY--;

        for (int y = minY; y < maxY; y++) {
            String row = rows.get(y);
            for (int x = 0; x < row.length(); x++) {
                if (row.charAt(x) != ' ') {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                }
            }
        }

        if (minX == Integer.MAX_VALUE) return new String[0];

        String[] result = new String[maxY - minY];
        for (int i = 0; i < result.length; i++) {
            result[i] = rows.get(i + minY).substring(minX, maxX + 1);
        }
        return result;
    }

    private static Codec<List<String>> patternCodec(int gridSize) {
        return Codec.STRING.listOf().comapFlatMap(rows -> {
            if (rows.isEmpty()) return DataResult.error(() -> "Invalid pattern: empty pattern not allowed");
            int width = rows.getFirst().length();
            for (String row : rows) {
                if (row.length() != width)
                    return DataResult.error(() -> "Invalid pattern: each row must be the same width");
            }
            String[] trimmed = trimPattern(rows);
            if (trimmed.length == 0) return DataResult.error(() -> "Invalid pattern: pattern is blank");
            if (trimmed.length > gridSize || trimmed[0].length() > gridSize)
                return DataResult.error(() -> "Pattern cannot exceed " + gridSize + "x" + gridSize);
            return DataResult.success(List.copyOf(rows));
        }, rows -> rows);
    }

    @FunctionalInterface
    public interface Factory<T extends AbstractShapedAlloyRecipe> {
        T create(String group, CraftingBookCategory category, List<String> pattern, Map<Character, Ingredient> key,
                 ItemStackTemplate output, float experience, int cookingTime);
    }

    public static <T extends AbstractShapedAlloyRecipe> MapCodec<T> mapCodec(int gridSize, Factory<T> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(r -> r.category),
                patternCodec(gridSize).fieldOf("pattern").forGetter(r -> r.rawPattern),
                RecipeCodecs.key(RecipeCodecs.INGREDIENT).fieldOf("key").forGetter(r -> r.key),
                RecipeCodecs.RESULT.fieldOf("result").forGetter(r -> r.output),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(r -> r.experience),
                Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(r -> r.cookingTime)
        ).apply(i, factory::create));
    }

    public static <T extends AbstractShapedAlloyRecipe> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(Factory<T> factory) {
        StreamCodec<RegistryFriendlyByteBuf, Map<Character, Ingredient>> keyCodec = ByteBufCodecs.map(
                LinkedHashMap::new,
                ByteBufCodecs.VAR_INT.map(i -> (char) i.intValue(), c -> (int) c),
                Ingredient.CONTENTS_STREAM_CODEC);
        return StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, r -> r.group,
                CraftingBookCategory.STREAM_CODEC, r -> r.category,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), r -> r.rawPattern,
                keyCodec, r -> r.key,
                ItemStackTemplate.STREAM_CODEC, r -> r.output,
                ByteBufCodecs.FLOAT, r -> r.experience,
                ByteBufCodecs.VAR_INT, r -> r.cookingTime,
                factory::create);
    }
}
