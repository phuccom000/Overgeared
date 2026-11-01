package net.stirdrem.overgeared.recipe;

import net.minecraft.core.registries.Registries;
import net.stirdrem.overgeared.registry.RegistrationProvider;
import net.stirdrem.overgeared.registry.RegistryEntry;


import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.recipe.castcooking.CastBlastingRecipe;
import net.stirdrem.overgeared.recipe.castcooking.CastSmeltingRecipe;
import net.stirdrem.overgeared.recipe.nbtcooking.NBTBlastingRecipe;
import net.stirdrem.overgeared.recipe.nbtcooking.NBTCampfireRecipe;
import net.stirdrem.overgeared.recipe.nbtcooking.NBTSmeltingRecipe;

public class ModRecipes {
    public static final RegistrationProvider<RecipeSerializer<?>> SERIALIZERS =
            RegistrationProvider.get(Registries.RECIPE_SERIALIZER);

    public static final RegistryEntry<RecipeSerializer<ForgingRecipe>> FORGING_SERIALIZER =
            SERIALIZERS.register("forging", () -> ForgingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<RockKnappingRecipe>> ROCK_KNAPPING_SERIALIZER =
            SERIALIZERS.register("rock_knapping", () -> RockKnappingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<OvergearedShapelessRecipe>> CRAFTING_SHAPELESS =
            SERIALIZERS.register("crafting_shapeless", () -> OvergearedShapelessRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<BlueprintCloningRecipe>> CRAFTING_BLUEPRINTCLONING =
            SERIALIZERS.register("crafting_cloning", () -> new SimpleCraftingRecipeSerializer<>(BlueprintCloningRecipe::new));
    public static final RegistryEntry<RecipeSerializer<DynamicToolCastRecipe>> CRAFTING_DYNAMIC_TOOL_CAST =
            SERIALIZERS.register("crafting_cast", () -> new SimpleCraftingRecipeSerializer<>(DynamicToolCastRecipe::new));
    public static final RegistryEntry<RecipeSerializer<ClayToolCastRecipe>> CLAY_TOOL_CAST =
            SERIALIZERS.register("crafting_initial_cast", () -> new SimpleCraftingRecipeSerializer<>(ClayToolCastRecipe::new));
    public static final RegistryEntry<RecipeSerializer<FletchingRecipe>> FLETCHING_SERIALIZER =
            SERIALIZERS.register("fletching", () -> FletchingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<NBTKeepingSmeltingRecipe>> NBT_SMELTING =
            SERIALIZERS.register("nbt_smelting", () -> NBTKeepingSmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<NBTKeepingBlastingRecipe>> NBT_BLASTING =
            SERIALIZERS.register("nbt_blasting", () -> NBTKeepingBlastingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<CastSmeltingRecipe>> CAST_SMELTING =
            SERIALIZERS.register("cast_smelting", () -> CastSmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<CastBlastingRecipe>> CAST_BLASTING =
            SERIALIZERS.register("cast_blasting", () -> CastBlastingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<AlloySmeltingRecipe>> ALLOY_SMELTING =
            SERIALIZERS.register("alloy_smelting", () -> AlloySmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<ShapedAlloySmeltingRecipe>> SHAPED_ALLOY_SMELTING =
            SERIALIZERS.register("shaped_alloy_smelting", () -> ShapedAlloySmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<NetherAlloySmeltingRecipe>> NETHER_ALLOY_SMELTING =
            SERIALIZERS.register("nether_alloy_smelting", () -> NetherAlloySmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<ShapedNetherAlloySmeltingRecipe>> SHAPED_NETHER_ALLOY_SMELTING =
            SERIALIZERS.register("shaped_nether_alloy_smelting", () -> ShapedNetherAlloySmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<ItemToToolTypeRecipe>> ITEM_TO_TOOLTYPE =
            SERIALIZERS.register("item_to_tooltype", ItemToToolTypeRecipe.Serializer::new);
    public static final RegistryEntry<RecipeSerializer<CoolingRecipe>> COOLING_SERIALIZER =
            SERIALIZERS.register("cooling", CoolingRecipe.Serializer::new);
    public static final RegistryEntry<RecipeSerializer<GrindingRecipe>> GRINDING_SERIALIZER =
            SERIALIZERS.register("grinding", GrindingRecipe.Serializer::new);
    public static final RegistryEntry<RecipeSerializer<CastingRecipe>> CASTING =
            SERIALIZERS.register("casting", () -> CastingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<NBTSmeltingRecipe>> NBT_ADD_SMELTING =
            SERIALIZERS.register("nbt_add_smelting", () -> NBTSmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<NBTBlastingRecipe>> NBT_ADD_BLASTING =
            SERIALIZERS.register("nbt_add_blasting", () -> NBTBlastingRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<NBTCampfireRecipe>> NBT_ADD_CAMPFIRE =
            SERIALIZERS.register("nbt_add_campfire_cooking", () -> NBTCampfireRecipe.Serializer.INSTANCE);

    /** Class-loads this registry. Call from the loader entrypoint. */
    public static void init() {
    }
}

