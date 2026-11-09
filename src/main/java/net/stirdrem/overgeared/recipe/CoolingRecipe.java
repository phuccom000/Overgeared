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
 * Quenching/cooling conversion. Input: {@link ItemListInput} slot 0 = the item being cooled.
 * JSON: {@code input} (ingredient), {@code output} (item stack).
 */
public class CoolingRecipe implements Recipe<ItemListInput> {
    private final Ingredient input;
    private final ItemStackTemplate output;

    public CoolingRecipe(Ingredient input, ItemStackTemplate output) {
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
    public RecipeSerializer<CoolingRecipe> getSerializer() {
        return ModRecipes.COOLING_SERIALIZER;
    }

    @Override
    public RecipeType<CoolingRecipe> getType() {
        return ModRecipeTypes.COOLING_RECIPE;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(input);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.COOLING;
    }

    public static class Type implements RecipeType<CoolingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "cooling";

        @Override
        public String toString() {
            return ID;
        }
    }

    public static final MapCodec<CoolingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RecipeCodecs.INGREDIENT.fieldOf("input").forGetter(r -> r.input),
            RecipeCodecs.RESULT.fieldOf("output").forGetter(r -> r.output)
    ).apply(i, CoolingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CoolingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
            ItemStackTemplate.STREAM_CODEC, r -> r.output,
            CoolingRecipe::new);

    public static final RecipeSerializer<CoolingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
