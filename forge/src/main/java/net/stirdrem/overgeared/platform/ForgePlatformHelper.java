package net.stirdrem.overgeared.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.config.ForgeServerConfig;
import net.stirdrem.overgeared.config.ServerConfigCommon;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.platform.services.IPlatformHelper;
import net.stirdrem.overgeared.recipe.ItemToToolTypeRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

public class ForgePlatformHelper implements IPlatformHelper {
    private final ServerConfigCommon config;

    public ForgePlatformHelper() {
        this.config = new ForgeServerConfig();
    }

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public ServerConfigCommon getConfig() {
        return config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Recipe<?>> RecipeSerializer<T> getRecipeSerializer(ResourceLocation id) {
        return (RecipeSerializer<T>) ForgeRegistries.RECIPE_SERIALIZERS.getValue(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Recipe<?>> RecipeType<T> getRecipeType(ResourceLocation id) {
        return (RecipeType<T>) ForgeRegistries.RECIPE_TYPES.getValue(id);
    }

    @Override
    public RecipeType<ItemToToolTypeRecipe> getItemToToolTypeRecipeType() {
        return ModRecipeTypes.ITEM_TO_TOOLTYPE.get();
    }

    @Override
    public EntityType<LingeringArrowEntity> getLingeringArrowEntity() {
        return ModEntities.LINGERING_ARROW.get();
    }

    @Override
    public EntityType<UpgradeArrowEntity> getUpgradeArrowEntity() {
        return ModEntities.UPGRADE_ARROW.get();
    }

    @Override
    public Item getIronArrow() {
        return ModItems.IRON_UPGRADE_ARROW.get();
    }

    @Override
    public Item getSteelArrow() {
        return ModItems.STEEL_UPGRADE_ARROW.get();
    }

    @Override
    public Item getDiamondArrow() {
        return ModItems.DIAMOND_UPGRADE_ARROW.get();
    }
}