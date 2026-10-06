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
import net.minecraft.world.item.crafting.BlastingRecipe;

/**
 * {@code overgeared:nbt_blasting}: a vanilla blasting recipe (type {@code minecraft:blasting}) whose result keeps
 * the input's data components (1.20.1: copied the input NBT). Vanilla blasting JSON; {@code cookingtime} defaults to 200.
 */
public class NBTKeepingBlastingRecipe extends BlastingRecipe implements CookingCodecs.ResultAccess {

    public NBTKeepingBlastingRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo,
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
    public RecipeSerializer<BlastingRecipe> getSerializer() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        RecipeSerializer<BlastingRecipe> s = (RecipeSerializer) ModRecipes.NBT_BLASTING;
        return s;
    }

    public static final MapCodec<NBTKeepingBlastingRecipe> MAP_CODEC = CookingCodecs.lenientCookingMapCodec(NBTKeepingBlastingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, NBTKeepingBlastingRecipe> STREAM_CODEC = AbstractCookingRecipe.cookingStreamCodec(NBTKeepingBlastingRecipe::new);
    public static final RecipeSerializer<NBTKeepingBlastingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
