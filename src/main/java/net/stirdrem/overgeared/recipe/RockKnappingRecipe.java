package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;
import net.stirdrem.overgeared.Overgeared;

public class RockKnappingRecipe implements Recipe<Inventory> {

    private final Identifier id;
    private final ItemStack output;
    private final Ingredient ingredient;

    private final boolean[][] pattern;
    private final int width;
    private final int height;
    private final boolean mirrored;


    /* ---------------- CONSTRUCTOR ---------------- */

    public RockKnappingRecipe(
            Identifier id,
            ItemStack output,
            Ingredient ingredient,
            boolean[][] pattern,
            int width,
            int height,
            boolean mirrored
    ) {
        this.id = id;
        this.output = output;
        this.ingredient = ingredient;
        this.pattern = pattern;
        this.width = width;
        this.height = height;
        this.mirrored = mirrored;
    }

    /* ---------------- MATCHING LOGIC ---------------- */

    @Override
    public boolean matches(Inventory inv, World world) {
        if (inv.size() != 9) return false;

        // Validate ingredient
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && !ingredient.test(stack)) {
                return false;
            }
        }

        boolean[][] input = new boolean[3][3];
        for (int i = 0; i < 9; i++) {
            input[i / 3][i % 3] = inv.getStack(i).isEmpty(); // true = chipped
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
                boolean inside =
                        x >= ox && x < ox + width &&
                                y >= oy && y < oy + height;

                if (!inside && input[y][x]) {
                    return false;
                }
            }
        }

        return true;
    }

    /* ---------------- RECIPE OUTPUT ---------------- */

    @Override
    public ItemStack craft(Inventory inv, DynamicRegistryManager access) {
        return output.copy();
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager access) {
        return output;
    }

    @Override
    public boolean fits(int w, int h) {
        return w == 3 && h == 3;
    }

    /* ---------------- GETTERS ---------------- */

    public boolean[][] getPattern() {
        return pattern;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    /* ---------------- RECIPE META ---------------- */

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ROCK_KNAPPING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.KNAPPING;
    }

    /* ---------------- TYPE ---------------- */

    public static class Type implements RecipeType<RockKnappingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "rock_knapping";
    }

    /* ---------------- SERIALIZER ---------------- */

    public static class Serializer implements RecipeSerializer<RockKnappingRecipe> {

        public static final Serializer INSTANCE = new Serializer();
        public static final Identifier ID =
                new Identifier(Overgeared.MOD_ID, "rock_knapping");

        @Override
        public RockKnappingRecipe read(Identifier id, JsonObject json) {
            ItemStack result =
                    ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));

            Ingredient ingredient =
                    Ingredient.fromJson(JsonHelper.getObject(json, "ingredient"));

            JsonArray patternArray = JsonHelper.getArray(json, "pattern");
            int height = patternArray.size();
            int width = patternArray.get(0).getAsString().length();

            boolean[][] pattern = new boolean[height][width];

            for (int y = 0; y < height; y++) {
                String row = JsonHelper.asString(patternArray.get(y), "pattern row");
                if (row.length() != width) {
                    throw new IllegalArgumentException("Pattern rows must be same width");
                }
                for (int x = 0; x < width; x++) {
                    char c = row.charAt(x);
                    pattern[y][x] = (c == 'x' || c == 'X');
                }
            }

            boolean mirrored = JsonHelper.getBoolean(json, "mirrored", false);

            return new RockKnappingRecipe(
                    id, result, ingredient, pattern,
                    width, height, mirrored
            );
        }

        @Override
        public void write(PacketByteBuf buf, RockKnappingRecipe r) {
            buf.writeItemStack(r.output);
            r.ingredient.write(buf);

            buf.writeVarInt(r.width);
            buf.writeVarInt(r.height);

            for (int y = 0; y < r.height; y++) {
                for (int x = 0; x < r.width; x++) {
                    buf.writeBoolean(r.pattern[y][x]);
                }
            }

            buf.writeBoolean(r.mirrored);
        }

        @Override
        public RockKnappingRecipe read(Identifier id, PacketByteBuf buf) {
            ItemStack output = buf.readItemStack();
            Ingredient ingredient = Ingredient.fromPacket(buf);

            int width = buf.readVarInt();
            int height = buf.readVarInt();

            boolean[][] pattern = new boolean[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pattern[y][x] = buf.readBoolean();
                }
            }

            boolean mirrored = buf.readBoolean();

            return new RockKnappingRecipe(
                    id, output, ingredient, pattern,
                    width, height, mirrored
            );
        }
    }
}
