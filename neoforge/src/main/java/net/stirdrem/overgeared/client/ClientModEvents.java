package net.stirdrem.overgeared.client;
import net.stirdrem.overgeared.recipe.RecipeLookup;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.renderer.SmithingAnvilBlockEntityRenderer;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.entity.renderer.LingeringArrowEntityRenderer;
import net.stirdrem.overgeared.entity.renderer.UpgradeArrowEntityRenderer;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.LingeringArrowItem;
import net.stirdrem.overgeared.item.custom.UpgradeArrowItem;
import net.stirdrem.overgeared.screen.*;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        OvergearedClient.init();
    }



    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.TIERED_SMITHING_ANVIL_MENU.get(), TieredSmithingAnvilScreen::new);
        event.register(ModMenuTypes.STONE_SMITHING_ANVIL_MENU.get(), StoneSmithingAnvilScreen::new);
        event.register(ModMenuTypes.ROCK_KNAPPING_MENU.get(), RockKnappingScreen::new);
        event.register(ModMenuTypes.BLUEPRINT_WORKBENCH_MENU.get(), BlueprintWorkbenchScreen::new);
        event.register(ModMenuTypes.FLETCHING_STATION_MENU.get(), FletchingStationScreen::new);
        event.register(ModMenuTypes.ALLOY_SMELTER_MENU.get(), AlloySmelterScreen::new);
        event.register(ModMenuTypes.NETHER_ALLOY_SMELTER_MENU.get(), NetherAlloySmelterScreen::new);
        event.register(ModMenuTypes.CASTING_SMELTER_MENU.get(), CastingSmelterScreen::new);
    }


    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LINGERING_ARROW.get(), LingeringArrowEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.UPGRADE_ARROW.get(), UpgradeArrowEntityRenderer::new);

        BlockEntityRendererProvider<AbstractSmithingAnvilBlockEntity, SmithingAnvilBlockEntityRenderer.State> anvilRenderer = SmithingAnvilBlockEntityRenderer::new;
        event.registerBlockEntityRenderer(ModBlockEntities.TIERED_SMITHING_ANVIL_BE.get(), anvilRenderer);
        event.registerBlockEntityRenderer(ModBlockEntities.STONE_SMITHING_ANVIL_BE.get(), anvilRenderer);
    }

    // Arrow tints and model variants: assets/overgeared/items/*.json.

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        // Register anvil minigame overlay below the hotbar
        event.registerBelow(VanillaGuiLayers.HOTBAR, AnvilMinigameOverlay.ID, AnvilMinigameOverlay.INSTANCE::render);
        // Register popup overlay above the minigame overlay
        event.registerAbove(AnvilMinigameOverlay.ID, PopupOverlay.ID, PopupOverlay.INSTANCE::render);
    }

    /**
     * Game bus events for the client.
     */
    @EventBusSubscriber(modid = OvergearedMod.MOD_ID, value = Dist.CLIENT)
    public static class GameBusEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            AnvilMinigameEvents.onClientTick();
        }

        /** The mod's recipes are synced by the server per connection (see RecipeLookup). */
        @SubscribeEvent
        public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            RecipeLookup.clearClientRecipes();
        }
    }
}
