package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.stirdrem.overgeared.util.ShapedAlloySerializerUtil;

import java.util.Map;

public class ShapedAlloySmeltingRecipe extends AbstractShapedAlloyRecipe implements IAlloyRecipe {

    public ShapedAlloySmeltingRecipe(
            Identifier id,
            String group,
            CraftingRecipeCategory category,
            int width,
            int height,
            DefaultedList<Ingredient> ingredients,
            ItemStack output,
            float experience,
            int cookingTime
    ) {
        super(id, group, category, width, height, 2, ingredients, output, experience, cookingTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SHAPED_ALLOY_SMELTING;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.SHAPED_ALLOY_SMELTING;
    }

    @Override
    public boolean isShaped() {
        return true;
    }

    public static class Type implements RecipeType<ShapedAlloySmeltingRecipe> {
        public static final ShapedAlloySmeltingRecipe.Type INSTANCE = new ShapedAlloySmeltingRecipe.Type();
        public static final String ID = "shaped_alloy_smelting";
    }

    public static class Serializer
            implements RecipeSerializer<ShapedAlloySmeltingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public ShapedAlloySmeltingRecipe read(Identifier id, JsonObject json) {
            String group = JsonHelper.getString(json, "group", "");
            CraftingRecipeCategory category =
                    json.has("category")
                            ? CraftingRecipeCategory.CODEC.byId(
                            json.get("category").getAsString(),
                            CraftingRecipeCategory.MISC)
                            : CraftingRecipeCategory.MISC;

            // 1) Parse & trim pattern
            JsonArray patternArray = JsonHelper.getArray(json, "pattern");
            var parsed = ShapedAlloySerializerUtil.parsePattern(patternArray, 2);

            // 2) Parse key
            Map<Character, Ingredient> key =
                    ShapedAlloySerializerUtil.parseKey(
                            JsonHelper.getObject(json, "key"));

            // 3) Build ingredient list
            DefaultedList<Ingredient> ingredients =
                    ShapedAlloySerializerUtil.buildIngredientList(
                            parsed.pattern(),
                            parsed.width(),
                            parsed.height(),
                            key
                    );

            // 4) Output & extras
            ItemStack output =
                    ShapedRecipe.outputFromJson(
                            JsonHelper.getObject(json, "result"));

            float experience = JsonHelper.getFloat(json, "experience", 0.0F);
            int cookingTime = JsonHelper.getInt(json, "cookingtime", 200);

            return new ShapedAlloySmeltingRecipe(
                    id,
                    group,
                    category,
                    parsed.width(),
                    parsed.height(),
                    ingredients,
                    output,
                    experience,
                    cookingTime
            );
        }

        // ---------------- NETWORK ----------------

        @Override
        public ShapedAlloySmeltingRecipe read(
                Identifier id,
                PacketByteBuf buf
        ) {
            String group = buf.readString();
            CraftingRecipeCategory category =
                    buf.readEnumConstant(CraftingRecipeCategory.class);

            int width = buf.readVarInt();
            int height = buf.readVarInt();

            DefaultedList<Ingredient> ingredients =
                    DefaultedList.ofSize(width * height, Ingredient.EMPTY);

            for (int i = 0; i < ingredients.size(); i++) {
                ingredients.set(i, Ingredient.fromPacket(buf));
            }

            ItemStack output = buf.readItemStack();
            float experience = buf.readFloat();
            int cookingTime = buf.readVarInt();

            return new ShapedAlloySmeltingRecipe(
                    id,
                    group,
                    category,
                    width,
                    height,
                    ingredients,
                    output,
                    experience,
                    cookingTime
            );
        }

        @Override
        public void write(
                PacketByteBuf buf,
                ShapedAlloySmeltingRecipe recipe
        ) {
            buf.writeString(recipe.getGroup());
            buf.writeEnumConstant(recipe.category());

            buf.writeVarInt(recipe.getWidth());
            buf.writeVarInt(recipe.getHeight());

            for (Ingredient ingredient : recipe.getIngredientsList()) {
                ingredient.write(buf);
            }

            buf.writeItemStack(recipe.getOutput(null));
            buf.writeFloat(recipe.getExperience());
            buf.writeVarInt(recipe.getCookingTime());
        }
    }
}
