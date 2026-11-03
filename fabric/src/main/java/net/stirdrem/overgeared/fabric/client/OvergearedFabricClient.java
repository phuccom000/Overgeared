package net.stirdrem.overgeared.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
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
import net.stirdrem.overgeared.item.armor.model.CopperHelmet;
import net.stirdrem.overgeared.item.armor.model.CopperLeggings;
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
        OvergearedClient.registerItemProperties();

        for (ModPackets.Packet<?> packet : ModPackets.CLIENTBOUND) {
            registerClientReceiver(packet);
        }

        registerScreens();
        registerRenderers();

        ColorProviderRegistry.ITEM.register(LingeringArrowItem::getColor, ModItems.LINGERING_ARROW.get());
        ColorProviderRegistry.ITEM.register(UpgradeArrowItem::getColor,
                ModItems.IRON_UPGRADE_ARROW.get(),
                ModItems.STEEL_UPGRADE_ARROW.get(),
                ModItems.DIAMOND_UPGRADE_ARROW.get());

        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            if (Minecraft.getInstance().options.hideGui) {
                return;
            }
            AnvilMinigameOverlay.INSTANCE.render(guiGraphics, deltaTracker);
            PopupOverlay.INSTANCE.render(guiGraphics, deltaTracker);
        });

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
        MenuScreens.register(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU.get(), SteelSmithingAnvilScreen::new);
        MenuScreens.register(ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU.get(), TierASmithingAnvilScreen::new);
        MenuScreens.register(ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU.get(), TierBSmithingAnvilScreen::new);
        MenuScreens.register(ModMenuTypes.STONE_SMITHING_ANVIL_MENU.get(), StoneSmithingAnvilScreen::new);
        MenuScreens.register(ModMenuTypes.ROCK_KNAPPING_MENU.get(), RockKnappingScreen::new);
        MenuScreens.register(ModMenuTypes.BLUEPRINT_WORKBENCH_MENU.get(), BlueprintWorkbenchScreen::new);
        MenuScreens.register(ModMenuTypes.FLETCHING_STATION_MENU.get(), FletchingStationScreen::new);
        MenuScreens.register(ModMenuTypes.ALLOY_SMELTER_MENU.get(), AlloySmelterScreen::new);
        MenuScreens.register(ModMenuTypes.NETHER_ALLOY_SMELTER_MENU.get(), NetherAlloySmelterScreen::new);
        MenuScreens.register(ModMenuTypes.CASTING_SMELTER_MENU.get(), CastingSmelterScreen::new);
    }

    private static void registerRenderers() {
        EntityModelLayerRegistry.registerModelLayer(CopperHelmet.LAYER_LOCATION, CopperHelmet::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CopperLeggings.LAYER_LOCATION, CopperLeggings::createBodyLayer);

        EntityRendererRegistry.register(ModEntities.LINGERING_ARROW.get(), LingeringArrowEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.UPGRADE_ARROW.get(), UpgradeArrowEntityRenderer::new);

        BlockEntityRenderers.register(ModBlockEntities.STEEL_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.STONE_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.TIER_A_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.TIER_B_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);

        registerArmorModel(OvergearedClient::copperHelmetModel, ModItems.COPPER_HELMET.get());
        registerArmorModel(OvergearedClient::copperLeggingsModel, ModItems.COPPER_LEGGINGS.get());
    }

    /**
     * Fabric version of the NeoForge {@code IClientItemExtensions#getHumanoidArmorModel} hook.
     */
    @SuppressWarnings("unchecked")
    private static void registerArmorModel(Supplier<HumanoidModel<?>> modelFactory, net.minecraft.world.item.Item item) {
        ArmorRenderer.register((poseStack, buffers, stack, entity, slot, light, contextModel) -> {
            HumanoidModel<LivingEntity> model = (HumanoidModel<LivingEntity>) modelFactory.get();
            contextModel.copyPropertiesTo(model);
            setPartVisibility(model, slot);
            ResourceLocation texture = ModArmorMaterials.COPPER.value().layers().getFirst().texture(slot == EquipmentSlot.LEGS);
            ArmorRenderer.renderPart(poseStack, buffers, light, stack, model, texture);
        }, item);
    }

    /** Same as {@code HumanoidArmorLayer#setPartVisibility}. */
    private static void setPartVisibility(HumanoidModel<?> model, EquipmentSlot slot) {
        model.setAllVisible(false);
        switch (slot) {
            case HEAD -> {
                model.head.visible = true;
                model.hat.visible = true;
            }
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            default -> {
            }
        }
    }
}
