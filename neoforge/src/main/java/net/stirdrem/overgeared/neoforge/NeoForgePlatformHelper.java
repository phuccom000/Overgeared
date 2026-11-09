package net.stirdrem.overgeared.neoforge;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.stirdrem.overgeared.platform.IPlatformHelper;
import net.stirdrem.overgeared.registry.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class NeoForgePlatformHelper implements IPlatformHelper {
    /** Set by the mod constructor before any register is handed over. */
    static IEventBus modBus;

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public java.nio.file.Path getConfigDir() {
        return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }

    @Override
    public boolean isPhysicalClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public <R> void register(DeferredRegister<R> register) {
        Objects.requireNonNull(modBus, "Mod event bus is not set yet");
        modBus.addListener(RegisterEvent.class, event -> event.register(register.getRegistryKey(), helper ->
                register.getEntriesWithFactories().forEach((holder, factory) -> helper.register(holder.getId(), factory.get()))));
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory) {
        return IMenuTypeExtension.create(factory::create);
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData) {
        player.openMenu(provider, extraData);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToAllPlayers(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    @Override
    public @Nullable MinecraftServer getCurrentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public CreativeModeTab.Builder creativeModeTabBuilder() {
        return CreativeModeTab.builder();
    }

    @Override
    public int getBurnTime(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING);
    }

    @Override
    public ItemStack getCraftingRemainder(ItemStack stack) {
        return stack.getCraftingRemainingItem();
    }

    @Override
    public MapCodec<Ingredient> ingredientMapCodec() {
        return Ingredient.MAP_CODEC_NONEMPTY;
    }

    @Override
    public void firePlayerCraftingEvent(Player player, ItemStack crafted, Container craftMatrix) {
        EventHooks.firePlayerCraftingEvent(player, crafted, craftMatrix);
    }

    @Override
    public void registerAlias(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>> registry,
                              net.minecraft.resources.ResourceLocation oldId, net.minecraft.resources.ResourceLocation newId) {
        Objects.requireNonNull(modBus, "Mod event bus is not set yet");
        modBus.addListener(RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(registry)) {
                event.getRegistry().addAlias(oldId, newId);
            }
        });
    }

    @Override
    public void modifyAnvilTiers(java.util.List<net.stirdrem.overgeared.AnvilTier.Definition> tiers) {
        // KubeJS loads its startup scripts in its constructor, which runs before ours (see neoforge.mods.toml)
        if (isModLoaded("kubejs")) {
            net.stirdrem.overgeared.compat.kubejs.OvergearedKubeJSPlugin.modifyAnvilTiers(tiers);
        }
    }
}
