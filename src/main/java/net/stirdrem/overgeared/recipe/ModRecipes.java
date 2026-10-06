package net.stirdrem.overgeared.recipe;

import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.recipe.castcooking.CastBlastingRecipe;
import net.stirdrem.overgeared.recipe.castcooking.CastSmeltingRecipe;
import net.stirdrem.overgeared.recipe.nbtcooking.NBTBlastingRecipe;
import net.stirdrem.overgeared.recipe.nbtcooking.NBTCampfireRecipe;
import net.stirdrem.overgeared.recipe.nbtcooking.NBTSmeltingRecipe;

/**
 * Recipe serializer registration. Every serializer is also registered with Fabric's
 * {@link RecipeSynchronization} so the recipes are available client-side (see {@link RecipeLookup}).
 */
public class ModRecipes {

    public static final RecipeSerializer<ForgingRecipe> FORGING_SERIALIZER =
            register("forging", ForgingRecipe.SERIALIZER);
    public static final RecipeSerializer<RockKnappingRecipe> ROCK_KNAPPING_SERIALIZER =
            register("rock_knapping", RockKnappingRecipe.SERIALIZER);
    public static final RecipeSerializer<OvergearedShapelessRecipe> CRAFTING_SHAPELESS =
            register("crafting_shapeless", OvergearedShapelessRecipe.SERIALIZER);
    public static final RecipeSerializer<BlueprintCloningRecipe> CRAFTING_BLUEPRINTCLONING =
            register("crafting_cloning", BlueprintCloningRecipe.SERIALIZER);
    public static final RecipeSerializer<DynamicToolCastRecipe> CRAFTING_DYNAMIC_TOOL_CAST =
            register("crafting_cast", DynamicToolCastRecipe.SERIALIZER);
    public static final RecipeSerializer<ClayToolCastRecipe> CLAY_TOOL_CAST =
            register("crafting_initial_cast", ClayToolCastRecipe.SERIALIZER);
    public static final RecipeSerializer<FletchingRecipe> FLETCHING_SERIALIZER =
            register("fletching", FletchingRecipe.SERIALIZER);
    public static final RecipeSerializer<NBTKeepingSmeltingRecipe> NBT_SMELTING =
            register("nbt_smelting", NBTKeepingSmeltingRecipe.SERIALIZER);
    public static final RecipeSerializer<NBTKeepingBlastingRecipe> NBT_BLASTING =
            register("nbt_blasting", NBTKeepingBlastingRecipe.SERIALIZER);
    public static final RecipeSerializer<CastSmeltingRecipe> CAST_SMELTING =
            register("cast_smelting", CastSmeltingRecipe.SERIALIZER);
    public static final RecipeSerializer<CastBlastingRecipe> CAST_BLASTING =
            register("cast_blasting", CastBlastingRecipe.SERIALIZER);
    public static final RecipeSerializer<AlloySmeltingRecipe> ALLOY_SMELTING =
            register("alloy_smelting", AlloySmeltingRecipe.SERIALIZER);
    public static final RecipeSerializer<ShapedAlloySmeltingRecipe> SHAPED_ALLOY_SMELTING =
            register("shaped_alloy_smelting", ShapedAlloySmeltingRecipe.SERIALIZER);
    public static final RecipeSerializer<NetherAlloySmeltingRecipe> NETHER_ALLOY_SMELTING =
            register("nether_alloy_smelting", NetherAlloySmeltingRecipe.SERIALIZER);
    public static final RecipeSerializer<ShapedNetherAlloySmeltingRecipe> SHAPED_NETHER_ALLOY_SMELTING =
            register("shaped_nether_alloy_smelting", ShapedNetherAlloySmeltingRecipe.SERIALIZER);
    public static final RecipeSerializer<ItemToToolTypeRecipe> ITEM_TO_TOOLTYPE =
            register("item_to_tooltype", ItemToToolTypeRecipe.SERIALIZER);
    public static final RecipeSerializer<CoolingRecipe> COOLING_SERIALIZER =
            register("cooling", CoolingRecipe.SERIALIZER);
    public static final RecipeSerializer<GrindingRecipe> GRINDING_SERIALIZER =
            register("grinding", GrindingRecipe.SERIALIZER);
    public static final RecipeSerializer<CastingRecipe> CASTING =
            register("casting", CastingRecipe.SERIALIZER);
    public static final RecipeSerializer<NBTSmeltingRecipe> NBT_ADD_SMELTING =
            register("nbt_add_smelting", NBTSmeltingRecipe.SERIALIZER);
    public static final RecipeSerializer<NBTBlastingRecipe> NBT_ADD_BLASTING =
            register("nbt_add_blasting", NBTBlastingRecipe.SERIALIZER);
    public static final RecipeSerializer<NBTCampfireRecipe> NBT_ADD_CAMPFIRE =
            register("nbt_add_campfire_cooking", NBTCampfireRecipe.SERIALIZER);

    private static <T extends Recipe<?>> RecipeSerializer<T> register(String name, RecipeSerializer<T> serializer) {
        RecipeSerializer<T> registered = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Overgeared.id(name), serializer);
        RecipeSynchronization.synchronizeRecipeSerializer(registered);
        return registered;
    }

    /** Call from the common mod initializer (loads this class, recipe types and recipe book categories). */
    public static void register() {
        ModRecipeBookCategories.register();
    }
}
