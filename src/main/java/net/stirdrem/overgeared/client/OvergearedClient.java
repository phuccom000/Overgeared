package net.stirdrem.overgeared.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.client.renderer.SmithingAnvilBlockEntityRenderer;
import net.stirdrem.overgeared.client.renderer.SmithingAnvilRenderState;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.entity.renderer.LingeringArrowEntityRenderer;
import net.stirdrem.overgeared.entity.renderer.UpgradeArrowEntityRenderer;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.screen.*;

/**
 * Client entrypoint.
 *
 * <p>26.3: item tints and model predicates (potion-tinted/tipped/lingering arrows, armor trims)
 * are no longer registered here - they live in the generated item model definitions under
 * assets/overgeared/items/ (see datagen/ModModelProvider).
 */
public class OvergearedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientModMessages.register();
        AnvilMinigameEvents.register();
        AnvilMinigameOverlay.register();
        PopupOverlay.register();
        OvergearedTooltipEvents.register();

        MenuScreens.register(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU, SteelSmithingAnvilScreen::new);
        MenuScreens.register(ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU, TierASmithingAnvilScreen::new);
        MenuScreens.register(ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU, TierBSmithingAnvilScreen::new);
        MenuScreens.register(ModMenuTypes.STONE_SMITHING_ANVIL_MENU, StoneSmithingAnvilScreen::new);
        MenuScreens.register(ModMenuTypes.ALLOY_SMELTER_MENU, AlloySmelterScreen::new);
        MenuScreens.register(ModMenuTypes.NETHER_ALLOY_SMELTER_MENU, NetherAlloySmelterScreen::new);
        MenuScreens.register(ModMenuTypes.CAST_FURNACE, CastFurnaceScreen::new);
        MenuScreens.register(ModMenuTypes.ROCK_KNAPPING_MENU, RockKnappingScreen::new);
        MenuScreens.register(ModMenuTypes.BLUEPRINT_WORKBENCH_MENU, BlueprintWorkbenchScreen::new);
        MenuScreens.register(ModMenuTypes.FLETCHING_STATION_MENU, FletchingStationScreen::new);

        registerAnvilRenderer(ModBlockEntities.STEEL_SMITHING_ANVIL_BE);
        registerAnvilRenderer(ModBlockEntities.TIER_A_SMITHING_ANVIL_BE);
        registerAnvilRenderer(ModBlockEntities.TIER_B_SMITHING_ANVIL_BE);
        registerAnvilRenderer(ModBlockEntities.STONE_SMITHING_ANVIL_BE);

        EntityRendererRegistry.register(ModEntities.LINGERING_ARROW, LingeringArrowEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.UPGRADE_ARROW, UpgradeArrowEntityRenderer::new);
    }

    /**
     * All four anvil tiers share one renderer targeting the abstract base type.
     */
    private static <E extends AbstractSmithingAnvilBlockEntity> void registerAnvilRenderer(BlockEntityType<E> type) {
        BlockEntityRendererProvider<AbstractSmithingAnvilBlockEntity, SmithingAnvilRenderState> provider =
                SmithingAnvilBlockEntityRenderer::new;
        BlockEntityRendererRegistry.<E, SmithingAnvilRenderState>register(type, provider);
    }
}
