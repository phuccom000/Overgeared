package net.stirdrem.overgeared.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Identifier;
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
        ClientModMessages.register();
        AnvilMinigameEvents.register();
        AnvilMinigameOverlay.register();
        PopupOverlay.register();
        OvergearedTooltipEvents.register();

        HandledScreens.register(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU, SteelSmithingAnvilScreen::new);
        HandledScreens.register(ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU, TierASmithingAnvilScreen::new);
        HandledScreens.register(ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU, TierBSmithingAnvilScreen::new);
        HandledScreens.register(ModMenuTypes.STONE_SMITHING_ANVIL_MENU, StoneSmithingAnvilScreen::new);
        HandledScreens.register(ModMenuTypes.ALLOY_SMELTER_MENU, AlloySmelterScreen::new);
        HandledScreens.register(ModMenuTypes.NETHER_ALLOY_SMELTER_MENU, NetherAlloySmelterScreen::new);
        HandledScreens.register(ModMenuTypes.CAST_FURNACE, CastFurnaceScreen::new);
        HandledScreens.register(ModMenuTypes.ROCK_KNAPPING_MENU, RockKnappingScreen::new);
        HandledScreens.register(ModMenuTypes.BLUEPRINT_WORKBENCH_MENU, BlueprintWorkbenchScreen::new);
        HandledScreens.register(ModMenuTypes.FLETCHING_STATION_MENU, FletchingStationScreen::new);

        registerAnvilRenderer(ModBlockEntities.STEEL_SMITHING_ANVIL_BE);
        registerAnvilRenderer(ModBlockEntities.TIER_A_SMITHING_ANVIL_BE);
        registerAnvilRenderer(ModBlockEntities.TIER_B_SMITHING_ANVIL_BE);
        registerAnvilRenderer(ModBlockEntities.STONE_SMITHING_ANVIL_BE);

        EntityModelLayerRegistry.registerModelLayer(CustomCopperHelmet.LAYER_LOCATION, CustomCopperHelmet::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CustomCopperLeggings.LAYER_LOCATION, CustomCopperLeggings::createBodyLayer);
        CopperArmorRenderer copperArmorRenderer = new CopperArmorRenderer();
        ArmorRenderer.register(copperArmorRenderer, ModItems.COPPER_HELMET);
        ArmorRenderer.register(copperArmorRenderer, ModItems.COPPER_LEGGINGS);

        EntityRendererRegistry.register(ModEntities.LINGERING_ARROW, LingeringArrowEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.UPGRADE_ARROW, UpgradeArrowEntityRenderer::new);

        registerArrowPotionTypeProvider(ModItems.IRON_UPGRADE_ARROW);
        registerArrowPotionTypeProvider(ModItems.STEEL_UPGRADE_ARROW);
        registerArrowPotionTypeProvider(ModItems.DIAMOND_UPGRADE_ARROW);

        // layer0 (the "*_head" texture, despite the name - see upgradeArrowModel in the Forge
        // datagen) is the tintable potion-coating layer; layer1 ("*_base") is pre-colored art
        // and always renders at full white (no tint).
        ItemColorProvider arrowColorProvider = (stack, tintIndex) ->
                tintIndex == 0 && stack.hasNbt() ? PotionUtil.getColor(stack) : 0xFFFFFFFF;
        ColorProviderRegistry.ITEM.register(arrowColorProvider,
                ModItems.IRON_UPGRADE_ARROW, ModItems.STEEL_UPGRADE_ARROW,
                ModItems.DIAMOND_UPGRADE_ARROW, ModItems.LINGERING_ARROW);
    }

    /**
     * Item model "overrides" predicate: 0 = plain arrow, 1 = tipped (Potion tag, no
     * LingeringPotion flag), 2 = lingering (Potion tag + LingeringPotion flag). Matches the
     * NBT written by FletchingStationScreenHandler when crafting tipped/lingering results.
     */
    private static void registerArrowPotionTypeProvider(Item item) {
        ModelPredicateProviderRegistry.register(item, new Identifier("overgeared", "potion_type"),
                (stack, world, entity, seed) -> {
                    NbtCompound nbt = stack.getNbt();
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
        BlockEntityRendererFactories.register(
                type,
                SmithingAnvilBlockEntityRenderer::new
        );
    }
}
