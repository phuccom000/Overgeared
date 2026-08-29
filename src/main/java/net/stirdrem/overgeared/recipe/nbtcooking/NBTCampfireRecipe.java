package net.stirdrem.overgeared.recipe.nbtcooking;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.stirdrem.overgeared.recipe.ModRecipes;
import net.stirdrem.overgeared.util.JsonToNBT;

public class NBTCampfireRecipe extends CampfireCookingRecipe {

    private final NbtCompound resultTag;

    public NBTCampfireRecipe(Identifier id, String group, CookingRecipeCategory category,
                              Ingredient ingredient, ItemStack result,
                              float xp, int time, NbtCompound tag) {
        super(id, group, category, ingredient, result, xp, time);
        this.resultTag = tag;
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryAccess) {
        ItemStack result = super.craft(inventory, registryAccess).copy();

        if (resultTag != null && !resultTag.isEmpty()) {
            result.getOrCreateNbt().copyFrom(resultTag);
        }

        return result;
    }

    public NbtCompound getResultTag() {
        return resultTag;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.NBT_ADD_CAMPFIRE;
    }

    public static class Serializer implements RecipeSerializer<NBTCampfireRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public NBTCampfireRecipe read(Identifier id, JsonObject json) {
            String group = JsonHelper.getString(json, "group", "");

            CookingRecipeCategory category = CookingRecipeCategory.CODEC.byId(
                    JsonHelper.getString(json, "category", "misc"),
                    CookingRecipeCategory.MISC
            );

            Ingredient ingredient = Ingredient.fromJson(
                    JsonHelper.getObject(json, "ingredient")
            );

            ItemStack result = ShapedRecipe.outputFromJson(
                    JsonHelper.getObject(json, "result")
            );

            float xp = JsonHelper.getFloat(json, "experience", 0.0f);
            int time = JsonHelper.getInt(json, "cookingtime", 200);

            NbtCompound tag = new NbtCompound();
            if (json.has("nbt")) {
                tag = JsonToNBT.parseCompound(
                        JsonHelper.getObject(json, "nbt")
                );
            }

            return new NBTCampfireRecipe(id, group, category, ingredient, result, xp, time, tag);
        }

        @Override
        public NBTCampfireRecipe read(Identifier id, PacketByteBuf buf) {
            String group = buf.readString();
            CookingRecipeCategory category = buf.readEnumConstant(CookingRecipeCategory.class);
            Ingredient ingredient = Ingredient.fromPacket(buf);
            ItemStack result = buf.readItemStack();
            float xp = buf.readFloat();
            int time = buf.readVarInt();
            NbtCompound tag = buf.readNbt();

            return new NBTCampfireRecipe(id, group, category, ingredient, result, xp, time, tag);
        }

        @Override
        public void write(PacketByteBuf buf, NBTCampfireRecipe recipe) {
            buf.writeString(recipe.getGroup());
            buf.writeEnumConstant(recipe.getCategory());
            recipe.getIngredients().get(0).write(buf);
            buf.writeItemStack(recipe.getOutput(null));
            buf.writeFloat(recipe.getExperience());
            buf.writeVarInt(recipe.getCookTime());
            buf.writeNbt(recipe.getResultTag());
        }
    }
}
