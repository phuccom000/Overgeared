package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Data-only recipe mapping items to an Overgeared tool type (used by {@code ConfigHelper.getToolTypeForItem}).
 * Input: {@link ItemListInput} slot 0. JSON: {@code item} (ingredient), {@code tooltype} (string).
 */
public class ItemToToolTypeRecipe implements Recipe<ItemListInput> {

    private final Ingredient input;
    private final String toolType;

    public ItemToToolTypeRecipe(Ingredient input, String toolType) {
        this.input = input;
        this.toolType = toolType;
    }

    public Ingredient getInput() {
        return input;
    }

    public String getToolType() {
        return toolType;
    }

    @Override
    public boolean matches(ItemListInput container, Level world) {
        return input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(ItemListInput container) {
        return ItemStack.EMPTY; // purely data-driven recipe
    }

    /** One stack per item matched by the input ingredient. */
    @SuppressWarnings("deprecation")
    public List<ItemStack> getItems() {
        return input.items().map(ItemStack::new).toList();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<ItemToToolTypeRecipe> getSerializer() {
        return ModRecipes.ITEM_TO_TOOLTYPE;
    }

    @Override
    public RecipeType<ItemToToolTypeRecipe> getType() {
        return ModRecipeTypes.ITEM_TO_TOOLTYPE;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.ITEM_TO_TOOLTYPE;
    }

    public static final MapCodec<ItemToToolTypeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RecipeCodecs.INGREDIENT.fieldOf("item").forGetter(r -> r.input),
            Codec.STRING.fieldOf("tooltype").forGetter(r -> r.toolType)
    ).apply(i, ItemToToolTypeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemToToolTypeRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
            ByteBufCodecs.STRING_UTF8, r -> r.toolType,
            ItemToToolTypeRecipe::new);

    public static final RecipeSerializer<ItemToToolTypeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
