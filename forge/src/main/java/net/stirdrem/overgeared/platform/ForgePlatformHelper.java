package net.stirdrem.overgeared.platform;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.config.ForgeServerConfig;
import net.stirdrem.overgeared.config.ServerConfigCommon;
import net.stirdrem.overgeared.item.custom.ForgeTongs;
import net.stirdrem.overgeared.platform.services.IPlatformHelper;

import java.util.List;
import java.util.function.Supplier;

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
    public net.minecraft.server.MinecraftServer getServer() {
        return net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public void openMenu(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.MenuProvider provider, net.minecraft.core.BlockPos pos) {
        net.minecraftforge.network.NetworkHooks.openScreen(player, provider, pos);
    }

    @Override
    public net.minecraft.world.inventory.RecipeBookType getForgingRecipeBookType() {
        return net.stirdrem.overgeared.OvergearedForge.FORGING;
    }

    @Override
    public void onPlayerCrafted(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.item.ItemStack crafted, net.minecraft.world.Container craftMatrix) {
        net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent(player, crafted, craftMatrix);
    }

    @Override
    public <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityType<T> createEntityType(
            String name, net.minecraft.world.entity.EntityType.EntityFactory<T> factory, net.minecraft.world.entity.MobCategory category,
            float width, float height, int trackingRange, int updateInterval) {
        return net.minecraft.world.entity.EntityType.Builder.of(factory, category)
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
}
