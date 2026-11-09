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
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipes;

/** {@code overgeared:nbt_add_blasting} - see {@link AbstractNBTCookingRecipe}. Type: {@code minecraft:blasting}. */
public class NBTBlastingRecipe extends BlastingRecipe implements AbstractNBTCookingRecipe {

    private final CompoundTag resultTag;

    public NBTBlastingRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo, Ingredient ingredient,
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
    public RecipeSerializer<BlastingRecipe> getSerializer() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        RecipeSerializer<BlastingRecipe> s = (RecipeSerializer) ModRecipes.NBT_ADD_BLASTING;
        return s;
    }

    public static final MapCodec<NBTBlastingRecipe> MAP_CODEC = AbstractNBTCookingRecipe.mapCodec(NBTBlastingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, NBTBlastingRecipe> STREAM_CODEC = AbstractNBTCookingRecipe.streamCodec(NBTBlastingRecipe::new);
    public static final RecipeSerializer<NBTBlastingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
