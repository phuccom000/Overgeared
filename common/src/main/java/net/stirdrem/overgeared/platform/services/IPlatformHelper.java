package net.stirdrem.overgeared.platform.services;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.stirdrem.overgeared.config.ServerConfigCommon;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import net.stirdrem.overgeared.recipe.ItemToToolTypeRecipe;

import java.util.List;
import java.util.function.Supplier;

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

    /**
     * Builds an entity type. Vanilla's EntityType.Builder#build logs "No data fixer registered" for modded entities
     * on Fabric, so each loader uses its own builder (Forge: vanilla builder, Fabric: FabricEntityTypeBuilder).
     */
    <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityType<T> createEntityType(
            String name, net.minecraft.world.entity.EntityType.EntityFactory<T> factory, net.minecraft.world.entity.MobCategory category,
            float width, float height, int trackingRange, int updateInterval);

    /** The recipe-book type of the smithing anvil (an extended vanilla enum constant on both loaders). */
    net.minecraft.world.inventory.RecipeBookType getForgingRecipeBookType();

    /** Fires the loader's "player crafted an item" event, if it has one (Forge does, Fabric doesn't). */
    default void onPlayerCrafted(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.item.ItemStack crafted, net.minecraft.world.Container craftMatrix) {
    }

    /**
     * Opens a menu whose type reads the block position from the opening packet
     * (Forge {@code NetworkHooks.openScreen}, Fabric {@code ExtendedScreenHandlerFactory}).
     */
    void openMenu(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.MenuProvider provider, net.minecraft.core.BlockPos pos);

    /** The running server, or null when none is running (e.g. on the title screen). */
    @org.jetbrains.annotations.Nullable
    net.minecraft.server.MinecraftServer getServer();



    /**
     * Creates a tool tier. Forge registers it with {@code TierSortingRegistry} (sorted between {@code after}
     * and {@code before}, mining gated by {@code needsTag}); Fabric relies on the needs_*_tool tags alone.
     */
    Tier createToolTier(String name, int level, int uses, float speed, float attackDamageBonus, int enchantability,
                        TagKey<Block> needsTag, Supplier<Ingredient> repairIngredient, List<Tier> after, List<Tier> before);

    /** Creates a tongs item; Forge adds its tool-action override. */
    Item createTongs(Tier tier, int attackDamageModifier, float attackSpeedModifier, Item.Properties properties);
}
