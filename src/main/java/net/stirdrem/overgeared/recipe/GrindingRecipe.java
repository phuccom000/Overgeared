package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Grindstone conversion. Input: {@link ItemListInput} slot 0 = the item being ground.
 * JSON: {@code input} (ingredient), {@code output} (item stack).
 */
public class GrindingRecipe implements Recipe<ItemListInput> {
    private final Ingredient input;
    private final ItemStackTemplate output;

    public GrindingRecipe(Ingredient input, ItemStackTemplate output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(ItemListInput container, Level world) {
        return input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(ItemListInput container) {
        return output.create();
    }

    /** A fresh copy of the output. */
    public ItemStack getResultItem() {
        return output.create();
    }

    /** @deprecated use {@link #getResultItem()}. */
    @Deprecated
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getResultItem();
    }

    public Ingredient getInput() {
        return input;
    }

    /** A fresh copy of the output (same as {@link #getResultItem()}). */
    public ItemStack getOutput() {
        return output.create();
    }

    public ItemStackTemplate output() {
        return output;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<GrindingRecipe> getSerializer() {
        return ModRecipes.GRINDING_SERIALIZER;
    }

    @Override
    public RecipeType<GrindingRecipe> getType() {
        return ModRecipeTypes.GRINDING_RECIPE;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(input);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.GRINDING;
    }

    public static class Type implements RecipeType<GrindingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "grinding";

        @Override
        public String toString() {
            return ID;
        }
    }

    public static final MapCodec<GrindingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RecipeCodecs.INGREDIENT.fieldOf("input").forGetter(r -> r.input),
            RecipeCodecs.RESULT.fieldOf("output").forGetter(r -> r.output)
    ).apply(i, GrindingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
            ItemStackTemplate.STREAM_CODEC, r -> r.output,
            GrindingRecipe::new);

    public static final RecipeSerializer<GrindingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
