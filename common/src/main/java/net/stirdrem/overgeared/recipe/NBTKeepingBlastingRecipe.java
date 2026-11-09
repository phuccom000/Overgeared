package net.stirdrem.overgeared.recipe;
import net.stirdrem.overgeared.recipe.ingredients.Ingredients;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStackTemplate;

import net.stirdrem.overgeared.recipe.ingredients.IngredientCodecs;
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

    // AbstractCookingRecipe keeps these private (and the result as an ItemStackTemplate) since 1.21.2
    protected final String group;
    protected final CookingBookCategory category;
    protected final Ingredient ingredient;
    protected final ItemStackTemplate result;
    protected final float experience;
    protected final int cookingTime;

    public NBTKeepingBlastingRecipe(String group, CookingBookCategory category, Ingredient ingredient,
                                    ItemStackTemplate result, float experience, int cookingTime) {
        super(new Recipe.CommonInfo(true), new AbstractCookingRecipe.CookingBookInfo(category, group), ingredient,
                result, experience, cookingTime);
        this.group = group;
        this.category = category;
        this.ingredient = input();
        this.result = result;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        ItemStack inputStack = input.getItem(0);
        ItemStack output = this.result.create();

        // Copy all data components from input to output (replaces NBT copying)
        output.applyComponents(inputStack.getComponents());

        return output;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<BlastingRecipe> getSerializer() {
        // The parent fixes the type parameter; the serializer is really a RecipeSerializer<NBTKeepingBlastingRecipe>.
        return (RecipeSerializer<BlastingRecipe>) (RecipeSerializer<?>) ModRecipeSerializers.NBT_BLASTING.get();
    }

    public static class Serializer {

        public static final MapCodec<NBTKeepingBlastingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                CookingBookCategory.CODEC.optionalFieldOf("category", CookingBookCategory.MISC).forGetter(r -> r.category),
                IngredientCodecs.LENIENT.fieldOf("ingredient").forGetter(r -> r.ingredient),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
                Codec.FLOAT.optionalFieldOf("experience", 0.0f).forGetter(r -> r.experience),
                Codec.INT.optionalFieldOf("cookingtime", 100).forGetter(r -> r.cookingTime)
        ).apply(instance, NBTKeepingBlastingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, NBTKeepingBlastingRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        private static NBTKeepingBlastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CookingBookCategory category = buffer.readEnum(CookingBookCategory.class);
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buffer);
            float xp = buffer.readFloat();
            int cookTime = buffer.readVarInt();

            return new NBTKeepingBlastingRecipe(group, category, ingredient, result, xp, cookTime);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, NBTKeepingBlastingRecipe recipe){
            buffer.writeUtf(recipe.group);
            buffer.writeEnum(recipe.category);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient);
            ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeFloat(recipe.experience);
            buffer.writeVarInt(recipe.cookingTime);
        }

        public MapCodec<NBTKeepingBlastingRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, NBTKeepingBlastingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
