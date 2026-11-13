package net.stirdrem.overgeared.fabric;
import net.stirdrem.overgeared.networking.packet.SyncRecipesS2CPacket;
import net.stirdrem.overgeared.datapack.JsonDataReloadListener;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.stirdrem.overgeared.recipe.ModRecipeSerializers;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.command.ModCommands;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.event.ReloadListeners;
import net.stirdrem.overgeared.event.platform.PlayerInteractEvent;
import net.stirdrem.overgeared.loot.LootInjections;
import net.stirdrem.overgeared.networking.ModPackets;
import net.stirdrem.overgeared.networking.PacketContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public final class OvergearedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        OvergearedMod.init();
        OvergearedMod.commonSetup();

        registerPackets();
        registerReloadListeners();
        registerLifecycleEvents();
        registerInteractionEvents();
        registerLoot();
        syncVanillaTypeRecipes();

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> ModCommands.register(dispatcher));
    }

    // ---- Recipe sync ----

    /**
     * Since 26.x recipes aren't sent to clients. JEI on Fabric only asks Fabric API to sync the
     * minecraft: serializers, so our recipes of vanilla types (shapeless crafting, furnace, blast
     * furnace and campfire variants) would be missing from JEI's vanilla categories. Our own recipe
     * types reach the client through SyncRecipesS2CPacket instead.
     */
    private static void syncVanillaTypeRecipes() {
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.CRAFTING_SHAPELESS.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.NBT_SMELTING.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.NBT_BLASTING.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.CAMPFIRE_HEATING.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.CAST_SMELTING.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.CAST_BLASTING.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.DRAGON_BREATH_BREWING.get());
    }

    // ---- Networking ----

    private static void registerPackets() {
        for (ModPackets.Packet<?> packet : ModPackets.CLIENTBOUND) {
            registerType(PayloadTypeRegistry.clientboundPlay(), packet);
        }
        for (ModPackets.Packet<?> packet : ModPackets.SERVERBOUND) {
            registerType(PayloadTypeRegistry.serverboundPlay(), packet);
            registerServerReceiver(packet);
        }
    }

    private static <T extends CustomPacketPayload> void registerType(
            PayloadTypeRegistry<net.minecraft.network.RegistryFriendlyByteBuf> registry, ModPackets.Packet<T> packet) {
        registry.register(packet.type(), packet.codec());
    }

    private static <T extends CustomPacketPayload> void registerServerReceiver(ModPackets.Packet<T> packet) {
        ServerPlayNetworking.registerGlobalReceiver(packet.type(), (payload, context) ->
                packet.handler().accept(payload, new PacketContext() {
                    @Override
                    public Player player() {
                        return context.player();
                    }

                    @Override
                    public void enqueueWork(Runnable task) {
                        context.server().execute(task);
                    }
                }));
    }

    // ---- Data ----

    private static void registerReloadListeners() {
        ResourceLoader loader = ResourceLoader.get(PackType.SERVER_DATA);
        for (ReloadListeners.Entry entry : ReloadListeners.SERVER_DATA) {
            PreparableReloadListener listener = entry.factory().get();
            loader.registerReloadListener(entry.id(), new PreparableReloadListener() {
                @Override
                public CompletableFuture<Void> reload(SharedState state, Executor taskExecutor,
                                                      PreparationBarrier barrier, Executor reloadExecutor) {
                    // The listeners decode tags, which needs the registries of this reload
                    JsonDataReloadListener.setRegistries(state.get(ResourceLoader.REGISTRY_LOOKUP_KEY));
                    return listener.reload(state, taskExecutor, barrier, reloadExecutor);
                }

                @Override
                public String getName() {
                    return listener.getName();
                }
            });
        }
    }

    private static void registerLoot() {
        // NeoForge adds these through global loot modifiers; the forging quality is applied in LootTableMixin.
        List<LootInjections.Injection> injections = LootInjections.all();
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            for (LootInjections.Injection injection : injections) {
                if (injection.table().equals(key.identifier())) {
                    tableBuilder.withPool(LootPool.lootPool()
                            .setRolls(ContextIntProviders.exactly(1))
                            .when(LootItemRandomChanceCondition.randomChance(injection.chance()))
                            .add(LootItem.lootTableItem(injection.item().get())));
                }
            }
        });
    }

    // ---- Lifecycle ----

    private static void registerLifecycleEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> FabricPlatformHelper.currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPING.register(ModEvents::onServerStopping);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> FabricPlatformHelper.currentServer = null);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // NeoForge fires the player tick for each player before the end of the server tick
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ModEvents.onPlayerTick(player);
            }
            ModEvents.onServerTick();
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> ModEvents.onPlayerJoinWorld(entity));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ModEvents.onPlayerJoinWorld(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> ModEvents.onPlayerDisconnect(handler.getPlayer()));
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) ->
                ModEvents.onPlayerChangedDimension(player));

        // Recipes are not synced to clients any more; send ours on join and on /reload (see RecipeLookup)
        // Quality attribute definitions are server data too; clients need them for item tooltips
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            SyncRecipesS2CPacket.sendTo(player);
            net.stirdrem.overgeared.networking.packet.SyncQualityAttributesS2CPacket.sendTo(player);
        });
    }

    // ---- Interactions ----

    private static void registerInteractionEvents() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (player.isSpectator()) {
                return InteractionResult.PASS;
            }
            PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, hitResult);
            ModItemInteractEvents.onRightClickBlock(event);

            if (event.isCanceled()) {
                return event.getCancellationResult();
            }
            if (event.getUseBlock().isFalse()) {
                // Fabric cannot skip only the block; run the item use ourselves, as NeoForge would.
                if (event.getUseItem().isFalse()) {
                    return InteractionResult.FAIL;
                }
                ItemStack stack = player.getItemInHand(hand);
                if (stack.isEmpty() || player.getCooldowns().isOnCooldown(stack)) {
                    return InteractionResult.FAIL;
                }
                return stack.useOn(new UseOnContext(player, hand, hitResult));
            }
            return InteractionResult.PASS;
        });

        List<Consumer<PlayerInteractEvent.RightClickItem>> itemHandlers = List.of(
                ModItemInteractEvents::onRightClickItem,
                ModItemInteractEvents::onArrowTipping,
                ModItemInteractEvents::onUsingKnappable);
        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (player.isSpectator()) {
                return InteractionResult.PASS;
            }
            PlayerInteractEvent.RightClickItem event = new PlayerInteractEvent.RightClickItem(player, hand);
            for (Consumer<PlayerInteractEvent.RightClickItem> handler : itemHandlers) {
                handler.accept(event);
                if (event.isCanceled()) {
                    return event.getCancellationResult();
                }
            }
            return InteractionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            PlayerInteractEvent.LeftClickBlock event = new PlayerInteractEvent.LeftClickBlock(player, hand, pos, direction);
            ModItemInteractEvents.onLeftClickBlock(event);
            return event.isCanceled() ? InteractionResult.FAIL : InteractionResult.PASS;
        });
    }
}
