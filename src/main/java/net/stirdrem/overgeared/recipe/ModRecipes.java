package net.stirdrem.overgeared.recipe;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.recipe.castcooking.CastBlastingRecipe;
import net.stirdrem.overgeared.recipe.castcooking.CastSmeltingRecipe;
import net.stirdrem.overgeared.recipe.nbtcooking.NBTBlastingRecipe;
import net.stirdrem.overgeared.recipe.nbtcooking.NBTCampfireRecipe;
import net.stirdrem.overgeared.recipe.nbtcooking.NBTSmeltingRecipe;

public class ModRecipes {

    public static final RecipeSerializer<ForgingRecipe> FORGING_SERIALIZER =
            register("forging", ForgingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<RockKnappingRecipe> ROCK_KNAPPING_SERIALIZER =
            register("rock_knapping", RockKnappingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<OvergearedShapelessRecipe> CRAFTING_SHAPELESS =
            register("crafting_shapeless", OvergearedShapelessRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<BlueprintCloningRecipe> CRAFTING_BLUEPRINTCLONING =
            register("crafting_cloning", new SpecialRecipeSerializer<>(BlueprintCloningRecipe::new));
    public static final RecipeSerializer<DynamicToolCastRecipe> CRAFTING_DYNAMIC_TOOL_CAST =
            register("crafting_cast", new SpecialRecipeSerializer<>(DynamicToolCastRecipe::new));
    public static final RecipeSerializer<ClayToolCastRecipe> CLAY_TOOL_CAST =
            register("crafting_initial_cast", new SpecialRecipeSerializer<>(ClayToolCastRecipe::new));
    public static final RecipeSerializer<FletchingRecipe> FLETCHING_SERIALIZER =
            register("fletching", FletchingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<NBTKeepingSmeltingRecipe> NBT_SMELTING =
            register("nbt_smelting", NBTKeepingSmeltingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<NBTKeepingBlastingRecipe> NBT_BLASTING =
            register("nbt_blasting", NBTKeepingBlastingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<CastSmeltingRecipe> CAST_SMELTING =
            register("cast_smelting", CastSmeltingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<CastBlastingRecipe> CAST_BLASTING =
            register("cast_blasting", CastBlastingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<AlloySmeltingRecipe> ALLOY_SMELTING =
            register("alloy_smelting", AlloySmeltingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<ShapedAlloySmeltingRecipe> SHAPED_ALLOY_SMELTING =
            register("shaped_alloy_smelting", ShapedAlloySmeltingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<NetherAlloySmeltingRecipe> NETHER_ALLOY_SMELTING =
            register("nether_alloy_smelting", NetherAlloySmeltingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<ShapedNetherAlloySmeltingRecipe> SHAPED_NETHER_ALLOY_SMELTING =
            register("shaped_nether_alloy_smelting", ShapedNetherAlloySmeltingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<ItemToToolTypeRecipe> ITEM_TO_TOOLTYPE =
            register("item_to_tooltype", new ItemToToolTypeRecipe.Serializer());
    public static final RecipeSerializer<CoolingRecipe> COOLING_SERIALIZER =
            register("cooling", new CoolingRecipe.Serializer());
    public static final RecipeSerializer<GrindingRecipe> GRINDING_SERIALIZER =
            register("grinding", new GrindingRecipe.Serializer());
    public static final RecipeSerializer<CastingRecipe> CASTING =
            register("casting", CastingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<NBTSmeltingRecipe> NBT_ADD_SMELTING =
            register("nbt_add_smelting", NBTSmeltingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<NBTBlastingRecipe> NBT_ADD_BLASTING =
            register("nbt_add_blasting", NBTBlastingRecipe.Serializer.INSTANCE);
    public static final RecipeSerializer<NBTCampfireRecipe> NBT_ADD_CAMPFIRE =
            register("nbt_add_campfire_cooking", NBTCampfireRecipe.Serializer.INSTANCE);

    private static <T extends net.minecraft.recipe.Recipe<?>> RecipeSerializer<T> register(String name, RecipeSerializer<T> serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Overgeared.id(name), serializer);
    }

    public static void register() {
    }
}
