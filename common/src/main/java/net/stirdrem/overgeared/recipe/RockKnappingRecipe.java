package net.stirdrem.overgeared.recipe;
import net.minecraft.world.item.ItemStackTemplate;

import net.stirdrem.overgeared.recipe.ingredients.IngredientCodecs;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class RockKnappingRecipe implements OvergearedRecipe<RecipeInput> {
    protected final ItemStackTemplate result;
    protected final Ingredient ingredient;
    protected final KnappingRecipePattern pattern;

    public RockKnappingRecipe(ItemStackTemplate result, Ingredient ingredient, KnappingRecipePattern pattern) {
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
    public ItemStack assemble(RecipeInput input) {
        return result.create();
    }


    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return result.create();
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result.create();
    }

    public KnappingRecipePattern getPattern() {
        return pattern;
    }

    @Override
    public RecipeSerializer<RockKnappingRecipe> getSerializer() {
        return ModRecipeSerializers.ROCK_KNAPPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<RockKnappingRecipe> getType() {
        return ModRecipeTypes.KNAPPING.get();
    }

    public static class Serializer {

        public static final MapCodec<RockKnappingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
                IngredientCodecs.LENIENT.fieldOf("ingredient").forGetter(r -> r.ingredient),
                KnappingRecipePattern.CODEC.forGetter(r -> r.pattern)
        ).apply(instance, RockKnappingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RockKnappingRecipe> STREAM_CODEC = StreamCodec.composite(
                ItemStackTemplate.STREAM_CODEC, r -> r.result,
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
                KnappingRecipePattern.STREAM_CODEC, r -> r.pattern,
                RockKnappingRecipe::new
        );

        public MapCodec<RockKnappingRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, RockKnappingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
