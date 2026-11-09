package net.stirdrem.overgeared.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.stirdrem.overgeared.registry.DeferredRegister;
import net.minecraft.world.item.crafting.RecipeType;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.function.Supplier;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, OvergearedMod.MOD_ID);

    public static final Supplier<RecipeType<ForgingRecipe>> FORGING =
            RECIPE_TYPES.register("forging", ModRecipeTypes::simple);
    public static final Supplier<RecipeType<RockKnappingRecipe>> KNAPPING =
            RECIPE_TYPES.register("rock_knapping", ModRecipeTypes::simple);
    public static final Supplier<RecipeType<FletchingRecipe>> FLETCHING =
            RECIPE_TYPES.register("fletching", ModRecipeTypes::simple);
    public static final Supplier<RecipeType<AlloySmeltingRecipe>> ALLOY_SMELTING =
            RECIPE_TYPES.register("alloy_smelting", ModRecipeTypes::simple);
    public static final Supplier<RecipeType<NetherAlloySmeltingRecipe>> NETHER_ALLOY_SMELTING =
            RECIPE_TYPES.register("nether_alloy_smelting", ModRecipeTypes::simple);
    public static final Supplier<RecipeType<ShapedAlloySmeltingRecipe>> SHAPED_ALLOY_SMELTING =
            RECIPE_TYPES.register("shaped_alloy_smelting", ModRecipeTypes::simple);
    public static final Supplier<RecipeType<ShapedNetherAlloySmeltingRecipe>> SHAPED_NETHER_ALLOY_SMELTING =
            RECIPE_TYPES.register("shaped_nether_alloy_smelting", ModRecipeTypes::simple);
    public static final Supplier<RecipeType<ItemToToolTypeRecipe>> ITEM_TO_TOOLTYPE =
            RECIPE_TYPES.register("item_to_tooltype", ModRecipeTypes::simple);
    public static final Supplier<RecipeType<CoolingRecipe>> COOLING_RECIPE =
            RECIPE_TYPES.register("cooling", ModRecipeTypes::simple);
    public static final Supplier<RecipeType<GrindingRecipe>> GRINDING_RECIPE =
            RECIPE_TYPES.register("grinding", ModRecipeTypes::simple);
    public static final Supplier<RecipeType<CastingRecipe>> CASTING =
            RECIPE_TYPES.register("casting", ModRecipeTypes::simple);

    public static void register() {
        RECIPE_TYPES.register();
    }

    private static <T extends Recipe<?>> RecipeType<T> simple(ResourceLocation id) {
        return new RecipeType<>() {
            @Override
            public String toString() {
                return id.toString();
            }
        };
    }
}
