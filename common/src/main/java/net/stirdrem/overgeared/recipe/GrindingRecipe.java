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

public class GrindingRecipe implements OvergearedRecipe<SingleRecipeInput> {
    private final Ingredient ingredient;
    private final ItemStackTemplate result;

    public GrindingRecipe(Ingredient ingredient, ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result.create();
    }


    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return result.create();
    }

    @Override
    public RecipeSerializer<GrindingRecipe> getSerializer() {
        return ModRecipeSerializers.GRINDING_SERIALIZER.get();
    }

    @Override
    public RecipeType<GrindingRecipe> getType() {
        return ModRecipeTypes.GRINDING_RECIPE.get();
    }

    public Ingredient getInput() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result.create();
    }

    public static class Serializer {
        public static final MapCodec<GrindingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IngredientCodecs.LENIENT.fieldOf("input").forGetter(r -> r.ingredient),
                ItemStackTemplate.CODEC.fieldOf("output").forGetter(r -> r.result)
        ).apply(instance, GrindingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
                ItemStackTemplate.STREAM_CODEC, r -> r.result,
                GrindingRecipe::new
        );

        public MapCodec<GrindingRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
