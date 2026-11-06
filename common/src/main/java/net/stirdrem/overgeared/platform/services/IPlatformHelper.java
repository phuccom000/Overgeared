package net.stirdrem.overgeared.platform.services;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
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
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.config.ServerConfigCommon;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import net.stirdrem.overgeared.recipe.ItemToToolTypeRecipe;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

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
    <T extends Entity> EntityType<T> createEntityType(
            String name, EntityType.EntityFactory<T> factory, MobCategory category,
            float width, float height, int trackingRange, int updateInterval);

    /** The recipe-book type of the smithing anvil (an extended vanilla enum constant on both loaders). */
    RecipeBookType getForgingRecipeBookType();

    /** Fires the loader's "player crafted an item" event, if it has one (Forge does, Fabric doesn't). */
    default void onPlayerCrafted(ServerPlayer player, ItemStack crafted, Container craftMatrix) {
    }

    /**
     * Opens a menu whose type reads the block position from the opening packet
     * (Forge {@code NetworkHooks.openScreen}, Fabric {@code ExtendedScreenHandlerFactory}).
     */
    void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos);

    /** The running server, or null when none is running (e.g. on the title screen). */
    @Nullable
    MinecraftServer getServer();



    /**
     * Creates a tool tier. Forge registers it with {@code TierSortingRegistry} (sorted between {@code after}
     * and {@code before}, mining gated by {@code needsTag}); Fabric relies on the needs_*_tool tags alone.
     */
    Tier createToolTier(String name, int level, int uses, float speed, float attackDamageBonus, int enchantability,
                        TagKey<Block> needsTag, Supplier<Ingredient> repairIngredient, List<Tier> after, List<Tier> before);

    /** Creates a tongs item; Forge adds its tool-action override. */
    Item createTongs(Tier tier, int attackDamageModifier, float attackSpeedModifier, Item.Properties properties);

    /** Client config: whether the smithing anvil screen shows the recipe book button. */
    boolean isAnvilRecipeBookEnabled();

    /** Furnace burn time of the stack in ticks; 0 if it is not a fuel. */
    int getBurnTime(ItemStack stack);

    /** What is left behind when the stack is used up in a recipe (e.g. an empty bucket). */
    ItemStack getCraftingRemainder(ItemStack stack);

    /**
     * A menu type whose client-side constructor gets the data written by {@link #openMenu} (the block position)
     * (Forge {@code IForgeMenuType}, Fabric {@code ExtendedScreenHandlerType}).
     */
    <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory);

    /** A menu type without extra data. Vanilla's constructor is not accessible from common code. */
    <T extends AbstractContainerMenu> MenuType<T> createSimpleMenuType(SimpleMenuFactory<T> factory);

    /** A block entity type for the given blocks. Vanilla's builder is not accessible from common code. */
    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityFactory<T> factory, Block... blocks);

    /** The game's config directory. */
    Path getConfigDir();

    /**
     * Makes {@code oldId} resolve to {@code newId} in the registry, so worlds saved before a rename keep their
     * blocks, items and block entities. Call from the mod entrypoint, before registration.
     */
    void registerAlias(ResourceKey<? extends Registry<?>> registry, ResourceLocation oldId, ResourceLocation newId);

    /** Lets other mods change the anvil tiers read from config before they are registered (KubeJS). */
    default void modifyAnvilTiers(List<AnvilTier.Definition> tiers) {
    }

    @FunctionalInterface
    interface SimpleMenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory inventory);
    }

    @FunctionalInterface
    interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory inventory, FriendlyByteBuf data);
    }
}
