package net.stirdrem.overgeared.client;

import net.fabricmc.api.ClientModInitializer;
import net.stirdrem.overgeared.OvergearedFabric;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.renderer.SmithingAnvilBlockEntityRenderer;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.entity.renderer.LingeringArrowEntityRenderer;
import net.stirdrem.overgeared.entity.renderer.UpgradeArrowEntityRenderer;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.armor.model.CustomCopperHelmet;
import net.stirdrem.overgeared.item.armor.model.CustomCopperLeggings;
import net.stirdrem.overgeared.screen.*;

public class OvergearedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OvergearedFabric.registerTieredAnvils();
        ClientModMessages.register();
        FabricAnvilMinigameEvents.register();
        AnvilMinigameOverlay.register();
        PopupOverlay.register();
        OvergearedTooltipEvents.register();

        MenuScreens.register(ModMenuTypes.TIERED_SMITHING_ANVIL_MENU.get(), TieredSmithingAnvilScreen::new);
        MenuScreens.register(ModMenuTypes.STONE_SMITHING_ANVIL_MENU.get(), StoneSmithingAnvilScreen::new);
        MenuScreens.register(ModMenuTypes.ALLOY_SMELTER_MENU.get(), AlloySmelterScreen::new);
        MenuScreens.register(ModMenuTypes.NETHER_ALLOY_SMELTER_MENU.get(), NetherAlloySmelterScreen::new);
        MenuScreens.register(ModMenuTypes.CAST_FURNACE.get(), CastFurnaceScreen::new);
        MenuScreens.register(ModMenuTypes.ROCK_KNAPPING_MENU.get(), RockKnappingScreen::new);
        MenuScreens.register(ModMenuTypes.BLUEPRINT_WORKBENCH_MENU.get(), BlueprintWorkbenchScreen::new);
        MenuScreens.register(ModMenuTypes.FLETCHING_STATION_MENU.get(), FletchingStationScreen::new);

        registerAnvilRenderer(ModBlockEntities.TIERED_SMITHING_ANVIL_BE.get());
        registerAnvilRenderer(ModBlockEntities.STONE_SMITHING_ANVIL_BE.get());

        EntityModelLayerRegistry.registerModelLayer(CustomCopperHelmet.LAYER_LOCATION, CustomCopperHelmet::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CustomCopperLeggings.LAYER_LOCATION, CustomCopperLeggings::createBodyLayer);
        CopperArmorRenderer copperArmorRenderer = new CopperArmorRenderer();
        ArmorRenderer.register(copperArmorRenderer, ModItems.COPPER_HELMET.get());
        ArmorRenderer.register(copperArmorRenderer, ModItems.COPPER_LEGGINGS.get());

        EntityRendererRegistry.register(ModEntities.LINGERING_ARROW.get(), LingeringArrowEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.UPGRADE_ARROW.get(), UpgradeArrowEntityRenderer::new);

        registerArrowPotionTypeProvider(ModItems.IRON_UPGRADE_ARROW.get());
        registerArrowPotionTypeProvider(ModItems.STEEL_UPGRADE_ARROW.get());
        registerArrowPotionTypeProvider(ModItems.DIAMOND_UPGRADE_ARROW.get());

        // layer0 (the "*_head" texture, despite the name - see upgradeArrowModel in the Forge
        // datagen) is the tintable potion-coating layer; layer1 ("*_base") is pre-colored art
        // and always renders at full white (no tint).
        ItemColor arrowColorProvider = (stack, tintIndex) ->
                tintIndex == 0 && stack.hasTag() ? PotionUtils.getColor(stack) : 0xFFFFFFFF;
        ColorProviderRegistry.ITEM.register(arrowColorProvider,
                ModItems.IRON_UPGRADE_ARROW.get(), ModItems.STEEL_UPGRADE_ARROW.get(),
                ModItems.DIAMOND_UPGRADE_ARROW.get(), ModItems.LINGERING_ARROW.get());
    }

    /**
     * Item model "overrides" predicate: 0 = plain arrow, 1 = tipped (Potion tag, no
     * LingeringPotion flag), 2 = lingering (Potion tag + LingeringPotion flag). Matches the
     * NBT written by FletchingStationMenu when crafting tipped/lingering results.
     */
    private static void registerArrowPotionTypeProvider(Item item) {
        ItemProperties.register(item, new ResourceLocation("overgeared", "potion_type"),
                (stack, world, entity, seed) -> {
                    CompoundTag nbt = stack.getTag();
                    if (nbt == null || !nbt.contains("Potion")) {
                        return 0f;
                    }
                    return nbt.getBoolean("LingeringPotion") ? 2f : 1f;
                });
    }

    /**
     * All four anvil tiers share one renderer targeting the abstract base type. Generics are
     * invariant, so BlockEntityRenderer<AbstractSmithingAnvilBlockEntity> can't be handed
     * directly to register(BlockEntityType<E>, BlockEntityRendererFactory<? super E>) for a
     * concrete E - the cast is safe since the renderer only ever touches the abstract type's API.
     */
    private static <E extends AbstractSmithingAnvilBlockEntity> void registerAnvilRenderer(
            BlockEntityType<E> type
    ) {
        BlockEntityRenderers.register(
                type,
                SmithingAnvilBlockEntityRenderer::new
        );
    }
}
