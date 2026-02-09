package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.stirdrem.overgeared.util.ShapedAlloySerializerUtil;

import java.util.Map;

public class ShapedNetherAlloySmeltingRecipe extends AbstractShapedAlloyRecipe implements INetherAlloyRecipe {

    public ShapedNetherAlloySmeltingRecipe(
            ResourceLocation id,
            String group,
            CraftingBookCategory category,
            int width,
            int height,
            NonNullList<Ingredient> ingredients,
            ItemStack output,
            float experience,
            int cookingTime
    ) {
        super(id, group, category, width, height, 3, ingredients, output, experience, cookingTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SHAPED_NETHER_ALLOY_SMELTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.SHAPED_NETHER_ALLOY_SMELTING.get();
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
        public ShapedNetherAlloySmeltingRecipe fromJson(
                ResourceLocation id,
                JsonObject json
        ) {
            String group = GsonHelper.getAsString(json, "group", "");
            CraftingBookCategory category =
                    json.has("category")
                            ? CraftingBookCategory.CODEC.byName(
                            json.get("category").getAsString(),
                            CraftingBookCategory.MISC)
                            : CraftingBookCategory.MISC;

            var parsed =
                    ShapedAlloySerializerUtil.parsePattern(
                            GsonHelper.getAsJsonArray(json, "pattern"),
                            3
                    );

            Map<Character, Ingredient> key =
                    ShapedAlloySerializerUtil.parseKey(
                            GsonHelper.getAsJsonObject(json, "key"));

            NonNullList<Ingredient> ingredients =
                    ShapedAlloySerializerUtil.buildIngredientList(
                            parsed.pattern(),
                            parsed.width(),
                            parsed.height(),
                            key
                    );

            ItemStack output =
                    ShapedRecipe.itemStackFromJson(
                            GsonHelper.getAsJsonObject(json, "result"));

            float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
            int cookingTime = GsonHelper.getAsInt(json, "cookingtime", 200);

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
        public ShapedNetherAlloySmeltingRecipe fromNetwork(
                ResourceLocation id,
                FriendlyByteBuf buf
        ) {
            String group = buf.readUtf();
            CraftingBookCategory category =
                    buf.readEnum(CraftingBookCategory.class);

            int width = buf.readVarInt();
            int height = buf.readVarInt();

            NonNullList<Ingredient> ingredients =
                    NonNullList.withSize(width * height, Ingredient.EMPTY);

            for (int i = 0; i < ingredients.size(); i++) {
                ingredients.set(i, Ingredient.fromNetwork(buf));
            }

            ItemStack output = buf.readItem();
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
        public void toNetwork(
                FriendlyByteBuf buf,
                ShapedNetherAlloySmeltingRecipe recipe
        ) {
            buf.writeUtf(recipe.getGroup());
            buf.writeEnum(recipe.category());

            buf.writeVarInt(recipe.getWidth());
            buf.writeVarInt(recipe.getHeight());

            for (Ingredient ingredient : recipe.getIngredientsList()) {
                ingredient.toNetwork(buf);
            }

            buf.writeItem(recipe.getResultItem(null));
            buf.writeFloat(recipe.getExperience());
            buf.writeVarInt(recipe.getCookingTime());
        }
    }
}
