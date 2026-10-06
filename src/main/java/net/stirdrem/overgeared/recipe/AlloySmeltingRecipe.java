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

/** Shapeless alloy smelter recipe: up to 4 ingredients, input slots 0..3. See {@link AbstractShapelessAlloyRecipe}. */
public class AlloySmeltingRecipe extends AbstractShapelessAlloyRecipe implements IAlloyRecipe {
    public static final int SLOT_COUNT = 4;

    public AlloySmeltingRecipe(String group, CraftingBookCategory category, List<Ingredient> inputs, ItemStackTemplate output, float experience, int cookingTime) {
        super(SLOT_COUNT, group, category, inputs, output, experience, cookingTime);
    }

    @Override
    public RecipeSerializer<AlloySmeltingRecipe> getSerializer() {
        return ModRecipes.ALLOY_SMELTING;
    }

    @Override
    public RecipeType<AlloySmeltingRecipe> getType() {
        return ModRecipeTypes.ALLOY_SMELTING;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.ALLOY_SMELTING;
    }

    public static class Type implements RecipeType<AlloySmeltingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "alloy_smelting";

        @Override
        public String toString() {
            return ID;
        }
    }

    public static final MapCodec<AlloySmeltingRecipe> MAP_CODEC = mapCodec(SLOT_COUNT, AlloySmeltingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, AlloySmeltingRecipe> STREAM_CODEC = streamCodec(AlloySmeltingRecipe::new);
    public static final RecipeSerializer<AlloySmeltingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
