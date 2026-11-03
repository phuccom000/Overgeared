package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class RockKnappingRecipe implements Recipe<RecipeInput> {
    protected final ItemStack result;
    protected final Ingredient ingredient;
    protected final KnappingRecipePattern pattern;

    public RockKnappingRecipe(ItemStack result, Ingredient ingredient, KnappingRecipePattern pattern) {
        this.result = result;
        this.ingredient = ingredient;
        this.pattern = pattern;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        if (input.size() != 9) return false;

        // Validate ingredient
        for (int i = 0; i < 9; i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && !ingredient.test(stack)) {
                return false;
            }
        }

        // Convert input into 3x3 grid (true = unchipped, false = chipped)
        boolean[][] inputGrid = new boolean[3][3];
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            inputGrid[row][col] = input.getItem(i).isEmpty(); // empty = chipped
        }
        return pattern.matches(inputGrid);
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider provider) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width == 3 && height == 3;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return result;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result;
    }

    public KnappingRecipePattern getPattern() {
        return pattern;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ROCK_KNAPPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.KNAPPING.get();
    }

    public static class Serializer implements RecipeSerializer<RockKnappingRecipe> {

        public static final MapCodec<RockKnappingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
                KnappingRecipePattern.CODEC.forGetter(r -> r.pattern)
        ).apply(instance, RockKnappingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RockKnappingRecipe> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, r -> r.result,
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
                KnappingRecipePattern.STREAM_CODEC, r -> r.pattern,
                RockKnappingRecipe::new
        );

        @Override
        public MapCodec<RockKnappingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RockKnappingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
