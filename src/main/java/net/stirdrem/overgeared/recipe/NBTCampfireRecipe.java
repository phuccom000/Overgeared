package net.stirdrem.overgeared.recipe;

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
            ItemStack result,
            float xp,
            int time
    ) {
        super(group, category, ingredient, result, xp, time);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries).copy();

        result.set(ModComponents.HEATED_COMPONENT, true);

        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CAMPFIRE_HEATING.get();
    }

    public static class Serializer implements RecipeSerializer<NBTCampfireRecipe> {

        public static final MapCodec<NBTCampfireRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),

                        CookingBookCategory.CODEC.optionalFieldOf("category", CookingBookCategory.MISC)
                                .forGetter(NBTCampfireRecipe::category),

                        Ingredient.CODEC.fieldOf("ingredient")
                                .forGetter(r -> r.getIngredients().get(0)),

                        ItemStack.CODEC.fieldOf("result")
                                .forGetter(r -> r.result),

                        Codec.FLOAT.optionalFieldOf("experience", 0.0f)
                                .forGetter(NBTCampfireRecipe::getExperience),

                        Codec.INT.optionalFieldOf("cookingtime", 200)
                                .forGetter(NBTCampfireRecipe::getCookingTime)
                ).apply(instance, NBTCampfireRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, NBTCampfireRecipe> STREAM_CODEC =
                StreamCodec.of(
                        Serializer::toNetwork,
                        Serializer::fromNetwork
                );

        @Override
        public MapCodec<NBTCampfireRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, NBTCampfireRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        // --- Network ---

        private static NBTCampfireRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();
            CookingBookCategory category = buf.readEnum(CookingBookCategory.class);
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            float xp = buf.readFloat();
            int time = buf.readVarInt();

            return new NBTCampfireRecipe(group, category, ingredient, result, xp, time);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, NBTCampfireRecipe recipe) {
            buf.writeUtf(recipe.getGroup());
            buf.writeEnum(recipe.category());
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getIngredients().get(0));
            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
            buf.writeFloat(recipe.getExperience());
            buf.writeVarInt(recipe.getCookingTime());
        }
    }
}