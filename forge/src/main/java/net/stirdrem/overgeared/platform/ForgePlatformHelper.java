package net.stirdrem.overgeared.platform;

import net.stirdrem.overgeared.config.ClientConfig;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.config.ForgeServerConfig;
import net.stirdrem.overgeared.config.ServerConfigCommon;
import net.stirdrem.overgeared.item.custom.ForgeTongs;
import net.stirdrem.overgeared.compat.kubejs.OvergearedKubeJSPlugin;
import net.stirdrem.overgeared.platform.services.IPlatformHelper;

import java.nio.file.Path;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.stirdrem.overgeared.OvergearedForge;

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
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public void registerAlias(ResourceKey<? extends Registry<?>> registry, ResourceLocation oldId, ResourceLocation newId) {
        ForgeRegistryAliases.add(registry, oldId, newId);
    }

    @Override
    public void modifyAnvilTiers(List<AnvilTier.Definition> tiers) {
        // Tiers are read when blocks register, after every mod (and KubeJS's startup scripts) has loaded
        if (isModLoaded("kubejs")) {
            OvergearedKubeJSPlugin.modifyAnvilTiers(tiers);
        }
    }

    @Override
    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        NetworkHooks.openScreen(player, provider, pos);
    }

    @Override
    public RecipeBookType getForgingRecipeBookType() {
        return OvergearedForge.FORGING;
    }

    @Override
    public void onPlayerCrafted(ServerPlayer player, ItemStack crafted, Container craftMatrix) {
        ForgeEventFactory.firePlayerCraftingEvent(player, crafted, craftMatrix);
    }

    @Override
    public <T extends Entity> EntityType<T> createEntityType(
            String name, EntityType.EntityFactory<T> factory, MobCategory category,
            float width, float height, int trackingRange, int updateInterval) {
        return EntityType.Builder.of(factory, category)
                .setShouldReceiveVelocityUpdates(true)
                .sized(width, height)
                .clientTrackingRange(trackingRange)
                .updateInterval(updateInterval)
                .build(name);
    }

    @Override
    public ServerConfigCommon getConfig() {
        return config;
    }


    @Override
    public Tier createToolTier(String name, int level, int uses, float speed, float attackDamageBonus, int enchantability,
                               TagKey<Block> needsTag, Supplier<Ingredient> repairIngredient, List<Tier> after, List<Tier> before) {
        return TierSortingRegistry.registerTier(
                new ForgeTier(level, uses, speed, attackDamageBonus, enchantability, needsTag, repairIngredient),
                Overgeared.id(name), List.copyOf(after), List.copyOf(before));
    }

    @Override
    public Item createTongs(Tier tier, int attackDamageModifier, float attackSpeedModifier, Item.Properties properties) {
        return new ForgeTongs(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public int getBurnTime(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    @Override
    public ItemStack getCraftingRemainder(ItemStack stack) {
        return stack.getCraftingRemainingItem();
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory) {
        return IForgeMenuType.create(factory::create);
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createSimpleMenuType(SimpleMenuFactory<T> factory) {
        return new MenuType<>(factory::create, FeatureFlags.VANILLA_SET);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityFactory<T> factory, Block... blocks) {
        return BlockEntityType.Builder.of(factory::create, blocks).build(null);
    }

    @Override
    public boolean isAnvilRecipeBookEnabled() {
        return ClientConfig.ENABLE_ANVIL_RECIPE_BOOK.get();
    }
}
