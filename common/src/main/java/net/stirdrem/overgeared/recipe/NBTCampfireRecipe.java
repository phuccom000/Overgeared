package net.stirdrem.overgeared.recipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.ItemStackTemplate;

import net.stirdrem.overgeared.recipe.ingredients.IngredientCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.stirdrem.overgeared.components.ModComponents;

public class NBTCampfireRecipe extends CampfireCookingRecipe {

    public NBTCampfireRecipe(
            String group,
            CookingBookCategory category,
            Ingredient ingredient,
            ItemStackTemplate result,
            float xp,
            int time
    ) {
        super(new Recipe.CommonInfo(true), new AbstractCookingRecipe.CookingBookInfo(category, group), ingredient,
                result, xp, time);
        this.result = result;
    }

    /** The result as written in the recipe (the parent only keeps an ItemStackTemplate). */
    private final ItemStackTemplate result;

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        ItemStack result = super.assemble(input).copy();

        result.set(ModComponents.HEATED_COMPONENT.get(), true);
        result.remove(ModComponents.FORGING_QUALITY.get());

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<CampfireCookingRecipe> getSerializer() {
        // The parent fixes the type parameter; the serializer is really a RecipeSerializer<NBTCampfireRecipe>.
        return (RecipeSerializer<CampfireCookingRecipe>) (RecipeSerializer<?>) ModRecipeSerializers.CAMPFIRE_HEATING.get();
    }

    public static class Serializer {

        public static final MapCodec<NBTCampfireRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::group),

                        CookingBookCategory.CODEC.optionalFieldOf("category", CookingBookCategory.MISC)
                                .forGetter(NBTCampfireRecipe::category),

                        IngredientCodecs.LENIENT.fieldOf("ingredient")
                                .forGetter(r -> r.input()),

                        ItemStackTemplate.CODEC.fieldOf("result")
                                .forGetter(r -> r.result),

                        Codec.FLOAT.optionalFieldOf("experience", 0.0f)
                                .forGetter(NBTCampfireRecipe::experience),

                        Codec.INT.optionalFieldOf("cookingtime", 200)
                                .forGetter(NBTCampfireRecipe::cookingTime)
                ).apply(instance, NBTCampfireRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, NBTCampfireRecipe> STREAM_CODEC =
                StreamCodec.of(
                        Serializer::toNetwork,
                        Serializer::fromNetwork
                );

        public MapCodec<NBTCampfireRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, NBTCampfireRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        // --- Network ---

        private static NBTCampfireRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();
            CookingBookCategory category = buf.readEnum(CookingBookCategory.class);
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
            float xp = buf.readFloat();
            int time = buf.readVarInt();

            return new NBTCampfireRecipe(group, category, ingredient, result, xp, time);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, NBTCampfireRecipe recipe) {
            buf.writeUtf(recipe.group());
            buf.writeEnum(recipe.category());
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input());
            ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result);
            buf.writeFloat(recipe.experience());
            buf.writeVarInt(recipe.cookingTime());
        }
    }
}