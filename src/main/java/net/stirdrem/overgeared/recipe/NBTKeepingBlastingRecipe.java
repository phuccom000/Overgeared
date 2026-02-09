package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class NBTKeepingBlastingRecipe extends BlastingRecipe {

    public NBTKeepingBlastingRecipe(String group, CookingBookCategory category, Ingredient ingredient,
                                    ItemStack result, float experience, int cookingTime) {
        super(group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider provider) {
        ItemStack inputStack = input.getItem(0);
        ItemStack output = this.result.copy();

        // Copy all data components from input to output (replaces NBT copying)
        output.applyComponents(inputStack.getComponents());

        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.NBT_BLASTING.get();
    }

    public static class Serializer implements RecipeSerializer<NBTKeepingBlastingRecipe> {

        private static final MapCodec<NBTKeepingBlastingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                CookingBookCategory.CODEC.optionalFieldOf("category", CookingBookCategory.MISC).forGetter(r -> r.category),
                Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                Codec.FLOAT.optionalFieldOf("experience", 0.0f).forGetter(r -> r.experience),
                Codec.INT.optionalFieldOf("cooking_time", 100).forGetter(r -> r.cookingTime)
        ).apply(instance, NBTKeepingBlastingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, NBTKeepingBlastingRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        private static NBTKeepingBlastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CookingBookCategory category = buffer.readEnum(CookingBookCategory.class);
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            float xp = buffer.readFloat();
            int cookTime = buffer.readVarInt();

            return new NBTKeepingBlastingRecipe(group, category, ingredient, result, xp, cookTime);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, NBTKeepingBlastingRecipe recipe){
            buffer.writeUtf(recipe.group);
            buffer.writeEnum(recipe.category);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeFloat(recipe.experience);
            buffer.writeVarInt(recipe.cookingTime);
        }

        @Override
        public MapCodec<NBTKeepingBlastingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, NBTKeepingBlastingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
