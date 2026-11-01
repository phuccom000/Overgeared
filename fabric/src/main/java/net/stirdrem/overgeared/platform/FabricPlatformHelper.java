package net.stirdrem.overgeared.platform;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.stirdrem.overgeared.config.FabricServerConfig;
import net.stirdrem.overgeared.config.ServerConfigCommon;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.platform.services.IPlatformHelper;
import net.stirdrem.overgeared.recipe.ItemToToolTypeRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

public class FabricPlatformHelper implements IPlatformHelper {
    private final ServerConfigCommon config;

    public FabricPlatformHelper() {
        this.config = new FabricServerConfig();
    }

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public ServerConfigCommon getConfig() {
        return config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Recipe<?>> RecipeSerializer<T> getRecipeSerializer(ResourceLocation id) {
        return (RecipeSerializer<T>) BuiltInRegistries.RECIPE_SERIALIZER.get(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Recipe<?>> RecipeType<T> getRecipeType(ResourceLocation id) {
        return (RecipeType<T>) BuiltInRegistries.RECIPE_TYPE.get(id);
    }

    @Override
    public RecipeType<ItemToToolTypeRecipe> getItemToToolTypeRecipeType() {
        return ModRecipeTypes.ITEM_TO_TOOLTYPE;
    }

    @Override
    public EntityType<LingeringArrowEntity> getLingeringArrowEntity() {
        return ModEntities.LINGERING_ARROW;
    }

    @Override
    public EntityType<UpgradeArrowEntity> getUpgradeArrowEntity() {
        return ModEntities.UPGRADE_ARROW;
    }

    @Override
    public Item getIronArrow() {
        return ModItems.IRON_UPGRADE_ARROW;
    }

    @Override
    public Item getSteelArrow() {
        return ModItems.STEEL_UPGRADE_ARROW;
    }

    @Override
    public Item getDiamondArrow() {
        return ModItems.DIAMOND_UPGRADE_ARROW;
    }
}
