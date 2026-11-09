package net.stirdrem.overgeared.recipe.nbtcooking;

import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
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
import net.stirdrem.overgeared.recipe.ModRecipes;

/** {@code overgeared:nbt_add_smelting} - see {@link AbstractNBTCookingRecipe}. Type: {@code minecraft:smelting}. */
public class NBTSmeltingRecipe extends SmeltingRecipe implements AbstractNBTCookingRecipe {

    private final CompoundTag resultTag;

    public NBTSmeltingRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo, Ingredient ingredient,
                             ItemStackTemplate result, float xp, int time, CompoundTag tag) {
        super(commonInfo, bookInfo, ingredient, result, xp, time);
        this.resultTag = tag == null ? new CompoundTag() : tag;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return AbstractNBTCookingRecipe.applyResultTag(result(), resultTag);
    }

    @Override
    public CompoundTag getResultTag() {
        return resultTag;
    }

    @Override
    public ItemStackTemplate resultTemplate() {
        return result();
    }

    @Override
    public RecipeSerializer<SmeltingRecipe> getSerializer() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        RecipeSerializer<SmeltingRecipe> s = (RecipeSerializer) ModRecipes.NBT_ADD_SMELTING;
        return s;
    }

    public static final MapCodec<NBTSmeltingRecipe> MAP_CODEC = AbstractNBTCookingRecipe.mapCodec(NBTSmeltingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, NBTSmeltingRecipe> STREAM_CODEC = AbstractNBTCookingRecipe.streamCodec(NBTSmeltingRecipe::new);
    public static final RecipeSerializer<NBTSmeltingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
