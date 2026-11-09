package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;

/**
 * {@code overgeared:nbt_smelting}: a vanilla smelting recipe (type {@code minecraft:smelting}) whose result keeps
 * the input's data components (1.20.1: copied the input NBT). Vanilla smelting JSON; {@code cookingtime} defaults to 200.
 */
public class NBTKeepingSmeltingRecipe extends SmeltingRecipe implements CookingCodecs.ResultAccess {

    public NBTKeepingSmeltingRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo,
                                    Ingredient ingredient, ItemStackTemplate result, float experience, int cookingTime) {
        super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return CookingCodecs.keepComponents(result(), input.item());
    }

    @Override
    public ItemStackTemplate resultTemplate() {
        return result();
    }

    @Override
    public RecipeSerializer<SmeltingRecipe> getSerializer() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        RecipeSerializer<SmeltingRecipe> s = (RecipeSerializer) ModRecipes.NBT_SMELTING;
        return s;
    }

    public static final MapCodec<NBTKeepingSmeltingRecipe> MAP_CODEC = CookingCodecs.lenientCookingMapCodec(NBTKeepingSmeltingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, NBTKeepingSmeltingRecipe> STREAM_CODEC = AbstractCookingRecipe.cookingStreamCodec(NBTKeepingSmeltingRecipe::new);
    public static final RecipeSerializer<NBTKeepingSmeltingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
