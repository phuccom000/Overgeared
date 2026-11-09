package net.stirdrem.overgeared.recipe;
import net.stirdrem.overgeared.recipe.ingredients.Ingredients;

import net.stirdrem.overgeared.recipe.ingredients.IngredientCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public record ItemToToolTypeRecipe(Ingredient input, String toolType) implements OvergearedRecipe<SingleRecipeInput> {

    @Override
    public boolean matches(SingleRecipeInput recipeInput, Level level) {
        return input.test(recipeInput.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput recipeInput) {
        return ItemStack.EMPTY; // purely data-driven recipe
    }


    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<ItemToToolTypeRecipe> getSerializer() {
        return ModRecipeSerializers.ITEM_TO_TOOLTYPE.get();
    }

    @Override
    public RecipeType<ItemToToolTypeRecipe> getType() {
        return ModRecipeTypes.ITEM_TO_TOOLTYPE.get();
    }

    public List<ItemStack> getItems() {
        return Ingredients.stacks(input);
    }

    // ----------------------------------------------------
    // Serializer
    // ----------------------------------------------------
    public static class Serializer {
        public static final MapCodec<ItemToToolTypeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IngredientCodecs.LENIENT.fieldOf("item").forGetter(r -> r.input),
                Codec.STRING.fieldOf("tooltype").forGetter(r -> r.toolType)
        ).apply(instance, ItemToToolTypeRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemToToolTypeRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
                ByteBufCodecs.STRING_UTF8, r -> r.toolType,
                ItemToToolTypeRecipe::new
        );

        public MapCodec<ItemToToolTypeRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, ItemToToolTypeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
