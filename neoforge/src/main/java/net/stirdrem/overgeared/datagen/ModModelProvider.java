package net.stirdrem.overgeared.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.renderer.item.properties.select.TrimMaterialProperty;
import net.minecraft.client.color.item.Potion;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.registry.DeferredItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Block states, block and item models, and item model definitions (assets/overgeared/items).
 * Replaces the pre-1.21.4 BlockStateProvider / ItemModelProvider.
 */
public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, OvergearedMod.MOD_ID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        // The drafting table's block state is hand-made (src/main/resources)
        return super.getKnownBlocks().filter(holder -> holder.value() != ModBlocks.DRAFTING_TABLE.get());
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        registerBlocks(blockModels);
        registerItems(itemModels);
    }

    // ---- blocks ------------------------------------------------------------------------------

    private void registerBlocks(BlockModelGenerators blockModels) {
        blockModels.createTrivialCube(ModBlocks.STEEL_BLOCK.get());

        // Tiered anvils use the hand-made model block/<tier>_smithing_anvil; their items reuse it
        ModBlocks.TIERED_ANVILS.forEach((tier, block) -> horizontalBlock(blockModels, block.get(), modLocation("block/" + tier.blockName())));
        horizontalBlock(blockModels, ModBlocks.STONE_SMITHING_ANVIL.get(), modLocation("block/" + AnvilTier.stone().blockName()));

        facingLitBlock(blockModels, ModBlocks.ALLOY_FURNACE.get(), "alloy_furnace", "alloy_furnace_on");
        facingLitBlock(blockModels, ModBlocks.NETHER_ALLOY_FURNACE.get(), "nether_alloy_furnace", "nether_alloy_furnace_on");
        facingLitBlock(blockModels, ModBlocks.CAST_FURNACE.get(), "casting_furnace", "casting_furnace_on");
    }

    private static void horizontalBlock(BlockModelGenerators blockModels, Block block, Identifier model) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blockModels.registerSimpleItemModel(block, model);
    }

    /** Hand-made off/on models; rotated by the facing like 1.21.1 (south unrotated). */
    private void facingLitBlock(BlockModelGenerators blockModels, Block block, String baseModelName, String litModelName) {
        Identifier base = modLocation("block/" + baseModelName);
        Identifier lit = modLocation("block/" + litModelName);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.LIT,
                        BlockModelGenerators.plainVariant(lit), BlockModelGenerators.plainVariant(base)))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING_ALT));
        blockModels.registerSimpleItemModel(block, base);
    }

    // ---- items -------------------------------------------------------------------------------

    private void registerItems(ItemModelGenerators itemModels) {
        flat(itemModels,
                ModItems.CRUDE_STEEL, ModItems.HEATED_CRUDE_STEEL, ModItems.ROCK, ModItems.STEEL_INGOT, ModItems.STEEL_NUGGET,
                ModItems.NETHERITE_ALLOY, ModItems.DIAMOND_SHARD, ModItems.IRON_ARROW_HEAD,
                ModItems.STEEL_ARROW_HEAD, ModItems.UNFIRED_TOOL_CAST, ModItems.CLAY_TOOL_CAST, ModItems.NETHER_TOOL_CAST,
                ModItems.HEATED_COPPER_INGOT, ModItems.HEATED_IRON_INGOT, ModItems.HEATED_STEEL_INGOT, ModItems.HEATED_SILVER_INGOT,
                ModItems.HEATED_NETHERITE_ALLOY, ModItems.COPPER_PLATE, ModItems.IRON_PLATE, ModItems.STEEL_PLATE,
                ModItems.STEEL_TONG, ModItems.IRON_TONG, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE, ModItems.EMPTY_BLUEPRINT,
                ModItems.BLUEPRINT,
                // tool parts
                ModItems.STONE_SWORD_BLADE, ModItems.IRON_SWORD_BLADE, ModItems.GOLDEN_SWORD_BLADE, ModItems.STEEL_SWORD_BLADE, ModItems.COPPER_SWORD_BLADE,
                ModItems.STONE_PICKAXE_HEAD, ModItems.IRON_PICKAXE_HEAD, ModItems.GOLDEN_PICKAXE_HEAD, ModItems.STEEL_PICKAXE_HEAD, ModItems.COPPER_PICKAXE_HEAD,
                ModItems.STONE_AXE_HEAD, ModItems.IRON_AXE_HEAD, ModItems.GOLDEN_AXE_HEAD, ModItems.STEEL_AXE_HEAD, ModItems.COPPER_AXE_HEAD,
                ModItems.STONE_SHOVEL_HEAD, ModItems.IRON_SHOVEL_HEAD, ModItems.GOLDEN_SHOVEL_HEAD, ModItems.STEEL_SHOVEL_HEAD, ModItems.COPPER_SHOVEL_HEAD,
                ModItems.STONE_HOE_HEAD, ModItems.IRON_HOE_HEAD, ModItems.GOLDEN_HOE_HEAD, ModItems.STEEL_HOE_HEAD, ModItems.COPPER_HOE_HEAD,
                ModItems.STONE_SPEAR_HEAD, ModItems.IRON_SPEAR_HEAD, ModItems.GOLDEN_SPEAR_HEAD, ModItems.STEEL_SPEAR_HEAD, ModItems.COPPER_SPEAR_HEAD);

        handheld(itemModels,
                ModItems.IRON_TONGS, ModItems.STEEL_TONGS, ModItems.WOODEN_TONGS,
                ModItems.STONE_HAMMER_HEAD, ModItems.COPPER_HAMMER_HEAD, ModItems.STEEL_HAMMER_HEAD,
                ModItems.SMITHING_HAMMER, ModItems.COPPER_SMITHING_HAMMER,
                ModItems.STEEL_SWORD, ModItems.STEEL_PICKAXE, ModItems.STEEL_AXE, ModItems.STEEL_SHOVEL, ModItems.STEEL_HOE);

        // 26.x spear: flat icon in the GUI, 3D model (item/steel_spear_in_hand texture) in hand
        itemModels.generateSpear(ModItems.STEEL_SPEAR.get());

        // Armor with trim variants (vanilla trim overlay textures)
        itemModels.generateTrimmableItem(ModItems.STEEL_HELMET.get(), ItemModelGenerators.TRIM_PREFIX_HELMET, false, java.util.Map.of());
        itemModels.generateTrimmableItem(ModItems.STEEL_CHESTPLATE.get(), ItemModelGenerators.TRIM_PREFIX_CHESTPLATE, false, java.util.Map.of());
        itemModels.generateTrimmableItem(ModItems.STEEL_LEGGINGS.get(), ItemModelGenerators.TRIM_PREFIX_LEGGINGS, false, java.util.Map.of());
        itemModels.generateTrimmableItem(ModItems.STEEL_BOOTS.get(), ItemModelGenerators.TRIM_PREFIX_BOOTS, false, java.util.Map.of());

        lingeringArrow(itemModels, ModItems.LINGERING_ARROW.get());
        upgradeArrow(itemModels, ModItems.IRON_UPGRADE_ARROW.get());
        upgradeArrow(itemModels, ModItems.STEEL_UPGRADE_ARROW.get());
        upgradeArrow(itemModels, ModItems.DIAMOND_UPGRADE_ARROW.get());
    }

    @SafeVarargs
    private static void flat(ItemModelGenerators itemModels, DeferredItem<? extends Item>... items) {
        for (DeferredItem<? extends Item> item : items) {
            itemModels.generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM);
        }
    }

    @SafeVarargs
    private static void handheld(ItemModelGenerators itemModels, DeferredItem<? extends Item>... items) {
        for (DeferredItem<? extends Item> item : items) {
            itemModels.generateFlatItem(item.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        }
    }

    /** The lingering arrow always shows its lingering head (potion tinted) and base. */
    private static void lingeringArrow(ItemModelGenerators itemModels, Item item) {
        Identifier model = itemModels.generateLayeredItem(item, TextureMapping.getItemTexture(item, "_head"), TextureMapping.getItemTexture(item, "_base"));
        itemModels.addPotionTint(item, model);
    }

    /**
     * Upgrade arrows: plain without a potion; with one, the tipped or (lingering status) lingering
     * variant, head layer tinted with the potion colour. Replaces the 1.21.1 "potion_type" property.
     */
    private static void upgradeArrow(ItemModelGenerators itemModels, Item item) {
        String name = ModelLocationUtils.getModelLocation(item).getPath().substring("item/".length());
        Identifier base = itemModels.createFlatItemModel(item, ModelTemplates.FLAT_ITEM);
        Identifier tipped = itemModels.generateLayeredItem(ModelLocationUtils.getModelLocation(item, "_tipped"),
                new Material(OvergearedMod.loc("item/tipped_" + name + "_head")), new Material(OvergearedMod.loc("item/tipped_" + name + "_base")));
        Identifier lingering = itemModels.generateLayeredItem(ModelLocationUtils.getModelLocation(item, "_lingering"),
                new Material(OvergearedMod.loc("item/lingering_" + name + "_head")), new Material(OvergearedMod.loc("item/lingering_" + name + "_base")));

        ItemModel.Unbaked withPotion = ItemModelUtils.select(new ComponentContents<>(ModComponents.LINGERING_STATUS.get()),
                ItemModelUtils.tintedModel(tipped, new Potion()),
                ItemModelUtils.when(true, ItemModelUtils.tintedModel(lingering, new Potion())));
        itemModels.itemModelOutput.accept(item, ItemModelUtils.conditional(ItemModelUtils.hasComponent(DataComponents.POTION_CONTENTS),
                withPotion, ItemModelUtils.plainModel(base)));
    }
}
