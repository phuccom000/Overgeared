package net.stirdrem.overgeared.fabric.client;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.stirdrem.overgeared.recipe.RecipeLookup;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.renderer.SmithingAnvilBlockEntityRenderer;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.stirdrem.overgeared.client.OvergearedClient;
import net.stirdrem.overgeared.client.PopupOverlay;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.entity.renderer.LingeringArrowEntityRenderer;
import net.stirdrem.overgeared.entity.renderer.UpgradeArrowEntityRenderer;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.item.ModArmorMaterials;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.LingeringArrowItem;
import net.stirdrem.overgeared.item.custom.UpgradeArrowItem;
import net.stirdrem.overgeared.networking.ModPackets;
import net.stirdrem.overgeared.networking.PacketContext;
import net.stirdrem.overgeared.screen.*;

import java.util.function.Supplier;

public final class OvergearedFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OvergearedClient.init();

        for (ModPackets.Packet<?> packet : ModPackets.CLIENTBOUND) {
            registerClientReceiver(packet);
        }

        registerScreens();
        registerRenderers();

        // Arrow potion tints and model variants are defined in assets/overgeared/items/*.json.
        HudElementRegistry.attachElementBefore(VanillaHudElements.HOTBAR, AnvilMinigameOverlay.ID, AnvilMinigameOverlay.INSTANCE::render);
        HudElementRegistry.attachElementAfter(AnvilMinigameOverlay.ID, PopupOverlay.ID, PopupOverlay.INSTANCE::render);

        // The mod's recipes are synced by the server per connection (see RecipeLookup)
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> RecipeLookup.clearClientRecipes());

        ClientTickEvents.END_CLIENT_TICK.register(client -> AnvilMinigameEvents.onClientTick());
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> ModEvents.onTooltip(stack, lines));
    }

    private static <T extends CustomPacketPayload> void registerClientReceiver(ModPackets.Packet<T> packet) {
        ClientPlayNetworking.registerGlobalReceiver(packet.type(), (payload, context) ->
                packet.handler().accept(payload, new PacketContext() {
                    @Override
                    public Player player() {
                        return context.player();
                    }

                    @Override
                    public void enqueueWork(Runnable task) {
                        context.client().execute(task);
                    }
                }));
    }

    private static void registerScreens() {
        MenuScreens.register(ModMenuTypes.TIERED_SMITHING_ANVIL_MENU.get(), TieredSmithingAnvilScreen::new);
        MenuScreens.register(ModMenuTypes.STONE_SMITHING_ANVIL_MENU.get(), StoneSmithingAnvilScreen::new);
        MenuScreens.register(ModMenuTypes.ROCK_KNAPPING_MENU.get(), RockKnappingScreen::new);
        MenuScreens.register(ModMenuTypes.BLUEPRINT_WORKBENCH_MENU.get(), BlueprintWorkbenchScreen::new);
        MenuScreens.register(ModMenuTypes.FLETCHING_STATION_MENU.get(), FletchingStationScreen::new);
        MenuScreens.register(ModMenuTypes.ALLOY_SMELTER_MENU.get(), AlloySmelterScreen::new);
        MenuScreens.register(ModMenuTypes.NETHER_ALLOY_SMELTER_MENU.get(), NetherAlloySmelterScreen::new);
        MenuScreens.register(ModMenuTypes.CASTING_SMELTER_MENU.get(), CastingSmelterScreen::new);
    }

    private static void registerRenderers() {
        EntityRendererRegistry.register(ModEntities.LINGERING_ARROW.get(), LingeringArrowEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.UPGRADE_ARROW.get(), UpgradeArrowEntityRenderer::new);

        BlockEntityRendererProvider<AbstractSmithingAnvilBlockEntity, SmithingAnvilBlockEntityRenderer.State> anvilRenderer = SmithingAnvilBlockEntityRenderer::new;
        BlockEntityRendererRegistry.register(ModBlockEntities.TIERED_SMITHING_ANVIL_BE.get(), anvilRenderer);
        BlockEntityRendererRegistry.register(ModBlockEntities.STONE_SMITHING_ANVIL_BE.get(), anvilRenderer);
    }
}
