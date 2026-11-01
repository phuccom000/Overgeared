package net.stirdrem.overgeared.platform;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.stirdrem.overgeared.item.custom.Tongs;

import java.util.List;
import java.util.function.Supplier;

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
    public net.minecraft.server.MinecraftServer getServer() {
        return net.stirdrem.overgeared.OvergearedFabric.getServer();
    }

    @Override
    public void openMenu(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.MenuProvider provider, net.minecraft.core.BlockPos pos) {
        player.openMenu(new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(net.minecraft.server.level.ServerPlayer p, net.minecraft.network.FriendlyByteBuf buf) {
                buf.writeBlockPos(pos);
            }

            @Override
            public net.minecraft.network.chat.Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inv, net.minecraft.world.entity.player.Player p) {
                return provider.createMenu(id, inv, p);
            }
        });
    }

    @Override
    public net.minecraft.world.inventory.RecipeBookType getForgingRecipeBookType() {
        return net.stirdrem.overgeared.recipebook.FabricRecipeBookTypes.forging();
    }

    @Override
    public <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityType<T> createEntityType(
            String name, net.minecraft.world.entity.EntityType.EntityFactory<T> factory, net.minecraft.world.entity.MobCategory category,
            float width, float height, int trackingRange, int updateInterval) {
        return net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder.<T>create(category, factory)
                .trackable(trackingRange, updateInterval, true)
                .dimensions(net.minecraft.world.entity.EntityDimensions.fixed(width, height))
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
}
