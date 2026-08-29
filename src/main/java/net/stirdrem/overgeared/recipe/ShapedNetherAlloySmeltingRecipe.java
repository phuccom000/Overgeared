package net.stirdrem.overgeared.recipe;

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

public class ShapedNetherAlloySmeltingRecipe extends AbstractShapedAlloyRecipe implements INetherAlloyRecipe {

    public ShapedNetherAlloySmeltingRecipe(
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
        super(id, group, category, width, height, 3, ingredients, output, experience, cookingTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SHAPED_NETHER_ALLOY_SMELTING;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.SHAPED_NETHER_ALLOY_SMELTING;
    }

    @Override
    public boolean isShaped() {
        return true;
    }

    public static class Type implements RecipeType<ShapedNetherAlloySmeltingRecipe> {
        public static final ShapedNetherAlloySmeltingRecipe.Type INSTANCE = new ShapedNetherAlloySmeltingRecipe.Type();
        public static final String ID = "shaped_nether_alloy_smelting";
    }

    public static class Serializer
            implements RecipeSerializer<ShapedNetherAlloySmeltingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public ShapedNetherAlloySmeltingRecipe read(
                Identifier id,
                JsonObject json
        ) {
            String group = JsonHelper.getString(json, "group", "");
            CraftingRecipeCategory category =
                    json.has("category")
                            ? CraftingRecipeCategory.CODEC.byId(
                            json.get("category").getAsString(),
                            CraftingRecipeCategory.MISC)
                            : CraftingRecipeCategory.MISC;

            var parsed =
                    ShapedAlloySerializerUtil.parsePattern(
                            JsonHelper.getArray(json, "pattern"),
                            3
                    );

            Map<Character, Ingredient> key =
                    ShapedAlloySerializerUtil.parseKey(
                            JsonHelper.getObject(json, "key"));

            DefaultedList<Ingredient> ingredients =
                    ShapedAlloySerializerUtil.buildIngredientList(
                            parsed.pattern(),
                            parsed.width(),
                            parsed.height(),
                            key
                    );

            ItemStack output =
                    ShapedRecipe.outputFromJson(
                            JsonHelper.getObject(json, "result"));

            float experience = JsonHelper.getFloat(json, "experience", 0.0F);
            int cookingTime = JsonHelper.getInt(json, "cookingtime", 200);

            return new ShapedNetherAlloySmeltingRecipe(
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

        @Override
        public ShapedNetherAlloySmeltingRecipe read(
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

            return new ShapedNetherAlloySmeltingRecipe(
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
                ShapedNetherAlloySmeltingRecipe recipe
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
