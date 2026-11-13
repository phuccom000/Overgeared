package net.stirdrem.overgeared.neoforge;
import net.stirdrem.overgeared.networking.packet.SyncRecipesS2CPacket;
import net.stirdrem.overgeared.datapack.JsonDataReloadListener;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.command.ModCommands;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.event.QualityAttributeHandler;
import net.stirdrem.overgeared.event.ReloadListeners;
import net.stirdrem.overgeared.event.platform.TriState;

import java.util.List;
import java.util.function.Consumer;

/**
 * Forwards NeoForge game events to the loader-independent handlers.
 */
@EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public final class NeoForgeGameEvents {
    private NeoForgeGameEvents() {
    }

    // ---- Registration-style game events ----

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void addReloadListeners(AddServerReloadListenersEvent event) {
        // The listeners decode tags, which needs the registries of this reload
        JsonDataReloadListener.setRegistries(event.getServerResources().fullRegistries().lookup());
        for (ReloadListeners.Entry entry : ReloadListeners.SERVER_DATA) {
            event.addListener(entry.id(), entry.factory().get());
        }
    }

    /** Recipes are not synced to clients any more; send ours on join and on /reload (see RecipeLookup). */
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach(SyncRecipesS2CPacket::sendTo);
        // Quality attribute definitions are server data too; clients need them for item tooltips
        event.getRelevantPlayers().forEach(net.stirdrem.overgeared.networking.packet.SyncQualityAttributesS2CPacket::sendTo);
    }

    // ---- Server / player lifecycle ----

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ModEvents.onServerTick();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        ModEvents.onPlayerTick(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        ModEvents.onPlayerJoinWorld(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ModEvents.onPlayerDisconnect(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ModEvents.onPlayerChangedDimension(event.getEntity());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ModEvents.onServerStopping(event.getServer());
    }

    // ---- Items ----

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ModEvents.onTooltip(event.getItemStack(), event.getToolTip());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        QualityAttributeHandler.applyQualityModifiers(stack, new QualityAttributeHandler.ModifierEditor() {
            @Override
            public List<ItemAttributeModifiers.Entry> getModifiers() {
                return event.getModifiers();
            }

            @Override
            public void removeModifier(Holder<Attribute> attribute, Identifier id) {
                event.removeModifier(attribute, id);
            }

            @Override
            public void addModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
                event.addModifier(attribute, modifier, slot);
            }
        });
        if (!net.stirdrem.overgeared.util.BrokenHelper.isBroken(stack)) {
            stack.set(ModComponents.FORGING_QUALITY.get(), stack.get(ModComponents.FORGING_QUALITY.get()));
        }
    }

    // Villager and wandering trader trades are JSON (data/overgeared/villager_trade); quality: VillagerTradeMixin.

    // ---- Interactions ----

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        var common = new net.stirdrem.overgeared.event.platform.PlayerInteractEvent.RightClickBlock(
                event.getEntity(), event.getHand(), event.getHitVec());
        ModItemInteractEvents.onRightClickBlock(common);

        if (!common.getUseItem().isDefault()) {
            event.setUseItem(toNeo(common.getUseItem()));
        }
        if (!common.getUseBlock().isDefault()) {
            event.setUseBlock(toNeo(common.getUseBlock()));
        }
        if (common.isCanceled()) {
            event.setCancellationResult(common.getCancellationResult());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        var common = new net.stirdrem.overgeared.event.platform.PlayerInteractEvent.RightClickItem(
                event.getEntity(), event.getHand());
        // These were separate listeners; like NeoForge, stop once one of them cancels.
        for (Consumer<net.stirdrem.overgeared.event.platform.PlayerInteractEvent.RightClickItem> handler : List.<Consumer<net.stirdrem.overgeared.event.platform.PlayerInteractEvent.RightClickItem>>of(
                ModItemInteractEvents::onRightClickItem,
                ModItemInteractEvents::onArrowTipping,
                ModItemInteractEvents::onUsingKnappable)) {
            handler.accept(common);
            if (common.isCanceled()) {
                event.setCancellationResult(common.getCancellationResult());
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        var common = new net.stirdrem.overgeared.event.platform.PlayerInteractEvent.LeftClickBlock(
                event.getEntity(), event.getHand(), event.getPos(), event.getFace());
        ModItemInteractEvents.onLeftClickBlock(common);
        if (common.isCanceled()) {
            event.setCanceled(true);
        }
    }

    private static net.minecraft.util.TriState toNeo(TriState state) {
        return switch (state) {
            case TRUE -> net.minecraft.util.TriState.TRUE;
            case FALSE -> net.minecraft.util.TriState.FALSE;
            case DEFAULT -> net.minecraft.util.TriState.DEFAULT;
        };
    }
}
