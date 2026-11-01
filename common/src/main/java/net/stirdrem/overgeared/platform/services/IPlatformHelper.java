package net.stirdrem.overgeared.platform.services;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.stirdrem.overgeared.config.ServerConfigCommon;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import net.stirdrem.overgeared.recipe.ItemToToolTypeRecipe;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    ServerConfigCommon getConfig();

    <T extends Recipe<?>> RecipeSerializer<T> getRecipeSerializer(ResourceLocation id);

    <T extends Recipe<?>> RecipeType<T> getRecipeType(ResourceLocation id);

    RecipeType<ItemToToolTypeRecipe> getItemToToolTypeRecipeType();

    EntityType<LingeringArrowEntity> getLingeringArrowEntity();

    EntityType<UpgradeArrowEntity> getUpgradeArrowEntity();

    Item getIronArrow();

    Item getSteelArrow();

    Item getDiamondArrow();

}