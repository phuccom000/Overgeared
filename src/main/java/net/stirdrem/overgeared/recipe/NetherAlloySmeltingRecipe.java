package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

/** Shapeless nether alloy smelter recipe: up to 9 ingredients, input slots 0..8. See {@link AbstractShapelessAlloyRecipe}. */
public class NetherAlloySmeltingRecipe extends AbstractShapelessAlloyRecipe implements INetherAlloyRecipe {
    public static final int SLOT_COUNT = 9;

    public NetherAlloySmeltingRecipe(String group, CraftingBookCategory category, List<Ingredient> inputs, ItemStackTemplate output, float experience, int cookingTime) {
        super(SLOT_COUNT, group, category, inputs, output, experience, cookingTime);
    }

    @Override
    public RecipeSerializer<NetherAlloySmeltingRecipe> getSerializer() {
        return ModRecipes.NETHER_ALLOY_SMELTING;
    }

    @Override
    public RecipeType<NetherAlloySmeltingRecipe> getType() {
        return ModRecipeTypes.NETHER_ALLOY_SMELTING;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.NETHER_ALLOY_SMELTING;
    }

    public static class Type implements RecipeType<NetherAlloySmeltingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "nether_alloy_smelting";

        @Override
        public String toString() {
            return ID;
        }
    }

    public static final MapCodec<NetherAlloySmeltingRecipe> MAP_CODEC = mapCodec(SLOT_COUNT, NetherAlloySmeltingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, NetherAlloySmeltingRecipe> STREAM_CODEC = streamCodec(NetherAlloySmeltingRecipe::new);
    public static final RecipeSerializer<NetherAlloySmeltingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
