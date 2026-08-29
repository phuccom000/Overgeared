package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class NBTKeepingSmeltingRecipe extends SmeltingRecipe {

    public NBTKeepingSmeltingRecipe(Identifier id, String group, CookingRecipeCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
        super(id, group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public ItemStack craft(Inventory inv, DynamicRegistryManager registryAccess) {
        ItemStack input = ItemStack.EMPTY;

        // Get input item
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && this.input.test(stack)) {
                input = stack.copy();
                break;
            }
        }

        ItemStack output = this.output.copy();

        // Copy NBT data
        if (input.hasNbt()) {
            output.setNbt(input.getNbt().copy());
        }

        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.NBT_SMELTING;
    }

    public static class Serializer implements RecipeSerializer<NBTKeepingSmeltingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public NBTKeepingSmeltingRecipe read(Identifier id, JsonObject json) {
            String group = JsonHelper.getString(json, "group", "");
            CookingRecipeCategory category = json.has("category")
                    ? CookingRecipeCategory.CODEC.byId(JsonHelper.getString(json, "category"), CookingRecipeCategory.MISC)
                    : CookingRecipeCategory.MISC;

            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));

            ItemStack result = json.get("result").isJsonObject()
                    ? ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"))
                    : new ItemStack(Registries.ITEM.get(new Identifier(JsonHelper.getString(json, "result"))));

            float xp = JsonHelper.getFloat(json, "experience", 0.0F);
            int cookTime = JsonHelper.getInt(json, "cookingtime", 200);

            return new NBTKeepingSmeltingRecipe(id, group, category, ingredient, result, xp, cookTime);
        }

        @Override
        public @Nullable NBTKeepingSmeltingRecipe read(Identifier id, PacketByteBuf buf) {
            String group = buf.readString();
            CookingRecipeCategory category = buf.readEnumConstant(CookingRecipeCategory.class);
            Ingredient ingredient = Ingredient.fromPacket(buf);
            ItemStack result = buf.readItemStack();
            float xp = buf.readFloat();
            int cookTime = buf.readVarInt();

            return new NBTKeepingSmeltingRecipe(id, group, category, ingredient, result, xp, cookTime);
        }

        @Override
        public void write(PacketByteBuf buf, NBTKeepingSmeltingRecipe recipe) {
            buf.writeString(recipe.getGroup());
            buf.writeEnumConstant(recipe.getCategory());
            recipe.input.write(buf);
            buf.writeItemStack(recipe.output);
            buf.writeFloat(recipe.experience);
            buf.writeVarInt(recipe.cookTime);
        }
    }

}
