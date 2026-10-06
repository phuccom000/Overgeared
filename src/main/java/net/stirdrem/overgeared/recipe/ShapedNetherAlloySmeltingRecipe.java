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
import java.util.Map;

/** Shaped nether alloy smelter recipe on the 3x3 input grid (slots 0..8). See {@link AbstractShapedAlloyRecipe}. */
public class ShapedNetherAlloySmeltingRecipe extends AbstractShapedAlloyRecipe implements INetherAlloyRecipe {
    public static final int GRID_SIZE = 3;

    public ShapedNetherAlloySmeltingRecipe(String group, CraftingBookCategory category, List<String> pattern, Map<Character, Ingredient> key,
                                           ItemStackTemplate output, float experience, int cookingTime) {
        super(GRID_SIZE, group, category, pattern, key, output, experience, cookingTime);
    }

    @Override
    public RecipeSerializer<ShapedNetherAlloySmeltingRecipe> getSerializer() {
        return ModRecipes.SHAPED_NETHER_ALLOY_SMELTING;
    }

    @Override
    public RecipeType<ShapedNetherAlloySmeltingRecipe> getType() {
        return ModRecipeTypes.SHAPED_NETHER_ALLOY_SMELTING;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.NETHER_ALLOY_SMELTING;
    }

    public static class Type implements RecipeType<ShapedNetherAlloySmeltingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "shaped_nether_alloy_smelting";

        @Override
        public String toString() {
            return ID;
        }
    }

    public static final MapCodec<ShapedNetherAlloySmeltingRecipe> MAP_CODEC = mapCodec(GRID_SIZE, ShapedNetherAlloySmeltingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedNetherAlloySmeltingRecipe> STREAM_CODEC = streamCodec(ShapedNetherAlloySmeltingRecipe::new);
    public static final RecipeSerializer<ShapedNetherAlloySmeltingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
