package net.stirdrem.overgeared.recipe.nbtcooking;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.stirdrem.overgeared.recipe.ModRecipes;
import net.stirdrem.overgeared.util.JsonToNBT;

public class NBTSmeltingRecipe extends AbstractNBTCookingRecipe {

    public NBTSmeltingRecipe(ResourceLocation id, String group, CookingBookCategory category,
                             Ingredient ingredient, ItemStack result,
                             float xp, int time, CompoundTag tag) {
        super(RecipeType.SMELTING, id, group, category, ingredient, result, xp, time, tag);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.NBT_ADD_SMELTING.get();
    }

    public static class Serializer implements RecipeSerializer<NBTSmeltingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public NBTSmeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");

            CookingBookCategory category = CookingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(json, "category", "misc"),
                    CookingBookCategory.MISC
            );

            Ingredient ingredient = Ingredient.fromJson(
                    GsonHelper.getAsJsonObject(json, "ingredient")
            );

            ItemStack result = ShapedRecipe.itemStackFromJson(
                    GsonHelper.getAsJsonObject(json, "result")
            );

            float xp = GsonHelper.getAsFloat(json, "experience", 0.0f);
            int time = GsonHelper.getAsInt(json, "cookingtime", 200);

            CompoundTag tag = new CompoundTag();
            if (json.has("nbt")) {
                tag = JsonToNBT.parseCompound(
                        GsonHelper.getAsJsonObject(json, "nbt")
                );
            }

            return new NBTSmeltingRecipe(id, group, category, ingredient, result, xp, time, tag);
        }

        @Override
        public NBTSmeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            CookingBookCategory category = buf.readEnum(CookingBookCategory.class);
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            float xp = buf.readFloat();
            int time = buf.readVarInt();
            CompoundTag tag = buf.readNbt();

            return new NBTSmeltingRecipe(id, group, category, ingredient, result, xp, time, tag);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, NBTSmeltingRecipe recipe) {
            buf.writeUtf(recipe.getGroup());
            buf.writeEnum(recipe.category());
            recipe.getIngredients().get(0).toNetwork(buf);
            buf.writeItem(recipe.getResultItem(null));
            buf.writeFloat(recipe.getExperience());
            buf.writeVarInt(recipe.getCookingTime());
            buf.writeNbt(recipe.getResultTag());
        }
    }
}