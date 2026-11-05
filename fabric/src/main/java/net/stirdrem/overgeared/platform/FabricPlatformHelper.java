package net.stirdrem.overgeared.platform;

import net.stirdrem.overgeared.config.ClientConfig;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.stirdrem.overgeared.item.custom.Tongs;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
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
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.stirdrem.overgeared.OvergearedFabric;
import net.stirdrem.overgeared.recipebook.FabricRecipeBookTypes;

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
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void registerAlias(ResourceKey<? extends Registry<?>> registry, ResourceLocation oldId, ResourceLocation newId) {
        RegistryAliases.add(registry, oldId, newId);
    }

    @Override
    public MinecraftServer getServer() {
        return OvergearedFabric.getServer();
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayer p, FriendlyByteBuf buf) {
                buf.writeBlockPos(pos);
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                return provider.createMenu(id, inv, p);
            }
        });
    }

    @Override
    public RecipeBookType getForgingRecipeBookType() {
        return FabricRecipeBookTypes.forging();
    }

    @Override
    public <T extends Entity> EntityType<T> createEntityType(
            String name, EntityType.EntityFactory<T> factory, MobCategory category,
            float width, float height, int trackingRange, int updateInterval) {
        return FabricEntityTypeBuilder.<T>create(category, factory)
                .trackable(trackingRange, updateInterval, true)
                .dimensions(EntityDimensions.fixed(width, height))
                .build();
    }

    @Override
    public ServerConfigCommon getConfig() {
        return config;
    }




    @Override
    public Tier createToolTier(String name, int level, int uses, float speed, float attackDamageBonus, int enchantability,
                               TagKey<Block> needsTag, Supplier<Ingredient> repairIngredient, List<Tier> after, List<Tier> before) {
        // No tier sorting on Fabric: ordering against vanilla tiers comes from the needs_*_tool tags
        return new SimpleTier(level, uses, speed, attackDamageBonus, enchantability, repairIngredient);
    }

    @Override
    public Item createTongs(Tier tier, int attackDamageModifier, float attackSpeedModifier, Item.Properties properties) {
        return new Tongs(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    private record SimpleTier(int level, int uses, float speed, float attackDamageBonus, int enchantability,
                              Supplier<Ingredient> repairIngredient) implements Tier {
        @Override public int getUses() { return uses; }
        @Override public float getSpeed() { return speed; }
        @Override public float getAttackDamageBonus() { return attackDamageBonus; }
        @Override public int getLevel() { return level; }
        @Override public int getEnchantmentValue() { return enchantability; }
        @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
    }

    @Override
    public int getBurnTime(ItemStack stack) {
        Integer time = FuelRegistry.INSTANCE.get(stack.getItem());
        return time == null ? 0 : time;
    }

    @Override
    public ItemStack getCraftingRemainder(ItemStack stack) {
        return stack.getRecipeRemainder();
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory) {
        return new ExtendedScreenHandlerType<>(factory::create);
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createSimpleMenuType(SimpleMenuFactory<T> factory) {
        return new MenuType<>(factory::create, FeatureFlags.VANILLA_SET);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityFactory<T> factory, Block... blocks) {
        return FabricBlockEntityTypeBuilder.create(factory::create, blocks).build();
    }

    @Override
    public boolean isAnvilRecipeBookEnabled() {
        return ClientConfig.ENABLE_ANVIL_RECIPE_BOOK.get();
    }
}
