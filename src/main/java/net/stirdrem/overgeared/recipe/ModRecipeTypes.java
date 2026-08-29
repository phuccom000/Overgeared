package net.stirdrem.overgeared.recipe;

import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.stirdrem.overgeared.Overgeared;

public class ModRecipeTypes {

    public static final RecipeType<ForgingRecipe> FORGING =
            register(ForgingRecipe.Type.ID, ForgingRecipe.Type.INSTANCE);
    public static final RecipeType<RockKnappingRecipe> KNAPPING =
            register(RockKnappingRecipe.Type.ID, RockKnappingRecipe.Type.INSTANCE);
    public static final RecipeType<FletchingRecipe> FLETCHING =
            register(FletchingRecipe.Type.ID, FletchingRecipe.Type.INSTANCE);
    public static final RecipeType<AlloySmeltingRecipe> ALLOY_SMELTING =
            register(AlloySmeltingRecipe.Type.ID, AlloySmeltingRecipe.Type.INSTANCE);
    public static final RecipeType<NetherAlloySmeltingRecipe> NETHER_ALLOY_SMELTING =
            register(NetherAlloySmeltingRecipe.Type.ID, NetherAlloySmeltingRecipe.Type.INSTANCE);
    public static final RecipeType<ShapedAlloySmeltingRecipe> SHAPED_ALLOY_SMELTING =
            register(ShapedAlloySmeltingRecipe.Type.ID, ShapedAlloySmeltingRecipe.Type.INSTANCE);
    public static final RecipeType<ShapedNetherAlloySmeltingRecipe> SHAPED_NETHER_ALLOY_SMELTING =
            register(ShapedNetherAlloySmeltingRecipe.Type.ID, ShapedNetherAlloySmeltingRecipe.Type.INSTANCE);
    public static final RecipeType<ItemToToolTypeRecipe> ITEM_TO_TOOLTYPE =
            register("item_to_tooltype", new RecipeType<ItemToToolTypeRecipe>() {
                @Override
                public String toString() {
                    return "item_to_tooltype";
                }
            });
    public static final RecipeType<CoolingRecipe> COOLING_RECIPE =
            register("cooling", CoolingRecipe.Type.INSTANCE);
    public static final RecipeType<GrindingRecipe> GRINDING_RECIPE =
            register("grinding", GrindingRecipe.Type.INSTANCE);
    public static final RecipeType<CastingRecipe> CASTING =
            register("casting", CastingRecipe.Type.INSTANCE);

    private static <T extends net.minecraft.recipe.Recipe<?>> RecipeType<T> register(String name, RecipeType<T> type) {
        return Registry.register(Registries.RECIPE_TYPE, Overgeared.id(name), type);
    }

    public static void register() {
    }
}
