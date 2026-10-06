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

/** Shaped alloy smelter recipe on the 2x2 input grid (slots 0..3). See {@link AbstractShapedAlloyRecipe}. */
public class ShapedAlloySmeltingRecipe extends AbstractShapedAlloyRecipe implements IAlloyRecipe {
    public static final int GRID_SIZE = 2;

    public ShapedAlloySmeltingRecipe(String group, CraftingBookCategory category, List<String> pattern, Map<Character, Ingredient> key,
                                     ItemStackTemplate output, float experience, int cookingTime) {
        super(GRID_SIZE, group, category, pattern, key, output, experience, cookingTime);
    }

    @Override
    public RecipeSerializer<ShapedAlloySmeltingRecipe> getSerializer() {
        return ModRecipes.SHAPED_ALLOY_SMELTING;
    }

    @Override
    public RecipeType<ShapedAlloySmeltingRecipe> getType() {
        return ModRecipeTypes.SHAPED_ALLOY_SMELTING;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.ALLOY_SMELTING;
    }

    public static class Type implements RecipeType<ShapedAlloySmeltingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "shaped_alloy_smelting";

        @Override
        public String toString() {
            return ID;
        }
    }

    public static final MapCodec<ShapedAlloySmeltingRecipe> MAP_CODEC = mapCodec(GRID_SIZE, ShapedAlloySmeltingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedAlloySmeltingRecipe> STREAM_CODEC = streamCodec(ShapedAlloySmeltingRecipe::new);
    public static final RecipeSerializer<ShapedAlloySmeltingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
