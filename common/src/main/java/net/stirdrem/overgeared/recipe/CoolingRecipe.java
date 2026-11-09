package net.stirdrem.overgeared.recipe;
import net.minecraft.world.item.ItemStackTemplate;

import net.stirdrem.overgeared.recipe.ingredients.IngredientCodecs;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public class CoolingRecipe implements OvergearedRecipe<SingleRecipeInput> {
    private final Ingredient ingredient;
    private final ItemStackTemplate result;

    public CoolingRecipe(Ingredient ingredient, ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.getItem(0));
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(ingredient);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result.create();
    }


    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return result.create();
    }

    @Override
    public RecipeSerializer<CoolingRecipe> getSerializer() {
        return ModRecipeSerializers.COOLING_SERIALIZER.get();
    }

    @Override
    public RecipeType<CoolingRecipe> getType() {
        return ModRecipeTypes.COOLING_RECIPE.get();
    }

    public static class Serializer {
        public static final MapCodec<CoolingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IngredientCodecs.LENIENT.fieldOf("input").forGetter(r -> r.ingredient),
                ItemStackTemplate.CODEC.fieldOf("output").forGetter(r -> r.result)
        ).apply(instance, CoolingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CoolingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
                ItemStackTemplate.STREAM_CODEC, r -> r.result,
                CoolingRecipe::new
        );

        public MapCodec<CoolingRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, CoolingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
