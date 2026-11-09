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
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipes;

/** {@code overgeared:nbt_add_campfire_cooking} - see {@link AbstractNBTCookingRecipe}. Type: {@code minecraft:campfire_cooking}. */
public class NBTCampfireRecipe extends CampfireCookingRecipe implements AbstractNBTCookingRecipe {

    private final CompoundTag resultTag;

    public NBTCampfireRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo, Ingredient ingredient,
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
    public RecipeSerializer<CampfireCookingRecipe> getSerializer() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        RecipeSerializer<CampfireCookingRecipe> s = (RecipeSerializer) ModRecipes.NBT_ADD_CAMPFIRE;
        return s;
    }

    public static final MapCodec<NBTCampfireRecipe> MAP_CODEC = AbstractNBTCookingRecipe.mapCodec(NBTCampfireRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, NBTCampfireRecipe> STREAM_CODEC = AbstractNBTCookingRecipe.streamCodec(NBTCampfireRecipe::new);
    public static final RecipeSerializer<NBTCampfireRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
