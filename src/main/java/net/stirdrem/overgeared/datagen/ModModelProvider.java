package net.stirdrem.overgeared.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.Potion;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;

import java.util.Map;

/**
 * Block states, block item models and item model definitions (assets/overgeared/items/*.json).
 *
 * <p>26.3 port notes:
 * <ul>
 *   <li>Item model "overrides" (overgeared:potion_type, trim_type) became item model definitions:
 *       arrows use {@code minecraft:condition}/{@code minecraft:has_component} on potion_contents and a
 *       {@code minecraft:select}/{@code minecraft:component} on overgeared:lingering_status; trims use
 *       the vanilla {@code minecraft:trim_material} select.</li>
 *   <li>The potion color (old ColorProviderRegistry ItemColor) is a {@code minecraft:potion} tint source
 *       on layer0 (the "head" texture); layer1 stays untinted.</li>
 *   <li>The drafting table keeps its hand-written blockstate/model in src/main/resources.</li>
 * </ul>
 */
public class ModModelProvider extends FabricModelProviderPlus {

    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    // -------------------------------------------------------------------------
    // BLOCK MODELS
    // -------------------------------------------------------------------------

    @Override
    public void generateBlockStateModels(BlockModelGenerators generator) {
        generator.createTrivialCube(ModBlocks.STEEL_BLOCK);

        horizontalBlock(generator, ModBlocks.SMITHING_ANVIL, modLoc("block/smithing_anvil"));
        horizontalBlock(generator, ModBlocks.TIER_A_SMITHING_ANVIL, modLoc("block/tier_a_smithing_anvil"));
        horizontalBlock(generator, ModBlocks.TIER_B_SMITHING_ANVIL, modLoc("block/tier_b_smithing_anvil"));
        horizontalBlock(generator, ModBlocks.STONE_SMITHING_ANVIL, modLoc("block/stone_anvil"));

        facingLitBlock(generator, ModBlocks.ALLOY_FURNACE, "alloy_furnace", "alloy_furnace_on");
        facingLitBlock(generator, ModBlocks.NETHER_ALLOY_FURNACE, "nether_alloy_furnace", "nether_alloy_furnace_on");
        facingLitBlock(generator, ModBlocks.CAST_FURNACE, "casting_furnace", "casting_furnace_on");

        // Blockstate is hand-written (src/main/resources); only the item definition is generated.
        blockItem(generator, ModBlocks.DRAFTING_TABLE, modLoc("block/drafting_table"));
    }

    private void horizontalBlock(BlockModelGenerators generator, Block block, Identifier model) {
        generator.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blockItem(generator, block, model);
    }

    private void facingLitBlock(BlockModelGenerators generator, Block block, String baseModelName, String litModelName) {
        Identifier baseModel = modLoc("block/" + baseModelName);
        MultiVariant normal = BlockModelGenerators.plainVariant(baseModel);
        MultiVariant lit = BlockModelGenerators.plainVariant(modLoc("block/" + litModelName));
        generator.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.LIT, lit, normal))
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blockItem(generator, block, baseModel);
    }

    // -------------------------------------------------------------------------
    // ITEM MODELS
    // -------------------------------------------------------------------------

    @Override
    public void generateItemModels(ItemModelGenerators generator) {
        // Simple items
        simpleItem(generator, ModItems.CRUDE_STEEL);
        simpleItem(generator, ModItems.HEATED_CRUDE_STEEL);
        simpleItem(generator, ModItems.ROCK);
        simpleItem(generator, ModItems.STEEL_INGOT);
        simpleItem(generator, ModItems.STEEL_NUGGET);
        simpleItem(generator, ModItems.NETHERITE_ALLOY);
        simpleItem(generator, ModItems.DIAMOND_SHARD);
        simpleItem(generator, ModItems.IRON_ARROW_HEAD);
        simpleItem(generator, ModItems.STEEL_ARROW_HEAD);
        simpleItem(generator, ModItems.UNFIRED_TOOL_CAST);
        simpleItem(generator, ModItems.CLAY_TOOL_CAST);
        simpleItem(generator, ModItems.NETHER_TOOL_CAST);

        // Arrows
        upgradeArrowModel(generator, ModItems.IRON_UPGRADE_ARROW);
        upgradeArrowModel(generator, ModItems.STEEL_UPGRADE_ARROW);
        upgradeArrowModel(generator, ModItems.DIAMOND_UPGRADE_ARROW);
        lingeringArrowModel(generator, ModItems.LINGERING_ARROW);

        // Heated metals
        simpleItem(generator, ModItems.HEATED_COPPER_INGOT);
        simpleItem(generator, ModItems.HEATED_IRON_INGOT);
        simpleItem(generator, ModItems.HEATED_STEEL_INGOT);
        simpleItem(generator, ModItems.HEATED_SILVER_INGOT);
        simpleItem(generator, ModItems.HEATED_NETHERITE_ALLOY);

        // Plates / miscellaneous
        simpleItem(generator, ModItems.COPPER_PLATE);
        simpleItem(generator, ModItems.IRON_PLATE);
        simpleItem(generator, ModItems.STEEL_PLATE);

        simpleItem(generator, ModItems.STEEL_TONG);
        simpleItem(generator, ModItems.IRON_TONG);

        simpleItem(generator, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE);
        simpleItem(generator, ModItems.EMPTY_BLUEPRINT);
        simpleItem(generator, ModItems.BLUEPRINT);

        // Armor (vanilla trim_material select)
        Map<net.minecraft.world.item.equipment.trim.TrimMaterials.Palette, net.minecraft.world.item.equipment.trim.TrimMaterials.Palette> noReplacements = Map.of();
        generator.generateTrimmableArmorSet(ModItems.STEEL_HELMET, ModItems.STEEL_CHESTPLATE,
                ModItems.STEEL_LEGGINGS, ModItems.STEEL_BOOTS, false, noReplacements);


        // Handheld items
        handheldItem(generator, ModItems.IRON_TONGS);
        handheldItem(generator, ModItems.STEEL_TONGS);
        handheldItem(generator, ModItems.WOODEN_TONGS);

        handheldItem(generator, ModItems.STONE_HAMMER_HEAD);
        handheldItem(generator, ModItems.COPPER_HAMMER_HEAD);
        handheldItem(generator, ModItems.STEEL_HAMMER_HEAD);

        handheldItem(generator, ModItems.SMITHING_HAMMER);
        handheldItem(generator, ModItems.COPPER_SMITHING_HAMMER);

        handheldItem(generator, ModItems.STEEL_SWORD);
        handheldItem(generator, ModItems.STEEL_PICKAXE);
        handheldItem(generator, ModItems.STEEL_AXE);
        handheldItem(generator, ModItems.STEEL_SHOVEL);
        generator.generateSpear(ModItems.STEEL_SPEAR); // 26.x spear: flat icon + 3D in-hand model
        handheldItem(generator, ModItems.STEEL_HOE);


        // Tool parts
        simpleItem(generator, ModItems.STONE_SWORD_BLADE);
        simpleItem(generator, ModItems.IRON_SWORD_BLADE);
        simpleItem(generator, ModItems.GOLDEN_SWORD_BLADE);
        simpleItem(generator, ModItems.STEEL_SWORD_BLADE);
        simpleItem(generator, ModItems.COPPER_SWORD_BLADE);

        simpleItem(generator, ModItems.STONE_PICKAXE_HEAD);
        simpleItem(generator, ModItems.IRON_PICKAXE_HEAD);
        simpleItem(generator, ModItems.GOLDEN_PICKAXE_HEAD);
        simpleItem(generator, ModItems.STEEL_PICKAXE_HEAD);
        simpleItem(generator, ModItems.COPPER_PICKAXE_HEAD);

        simpleItem(generator, ModItems.STONE_AXE_HEAD);
        simpleItem(generator, ModItems.IRON_AXE_HEAD);
        simpleItem(generator, ModItems.GOLDEN_AXE_HEAD);
        simpleItem(generator, ModItems.STEEL_AXE_HEAD);
        simpleItem(generator, ModItems.COPPER_AXE_HEAD);

        simpleItem(generator, ModItems.STONE_SHOVEL_HEAD);
        simpleItem(generator, ModItems.IRON_SHOVEL_HEAD);
        simpleItem(generator, ModItems.GOLDEN_SHOVEL_HEAD);
        simpleItem(generator, ModItems.STEEL_SHOVEL_HEAD);
        simpleItem(generator, ModItems.COPPER_SHOVEL_HEAD);
        simpleItem(generator, ModItems.STONE_SPEAR_HEAD);
        simpleItem(generator, ModItems.IRON_SPEAR_HEAD);
        simpleItem(generator, ModItems.GOLDEN_SPEAR_HEAD);
        simpleItem(generator, ModItems.STEEL_SPEAR_HEAD);
        simpleItem(generator, ModItems.COPPER_SPEAR_HEAD);

        simpleItem(generator, ModItems.STONE_HOE_HEAD);
        simpleItem(generator, ModItems.IRON_HOE_HEAD);
        simpleItem(generator, ModItems.GOLDEN_HOE_HEAD);
        simpleItem(generator, ModItems.STEEL_HOE_HEAD);
        simpleItem(generator, ModItems.COPPER_HOE_HEAD);
    }

    private void simpleItem(ItemModelGenerators generator, Item item) {
        generator.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    private void handheldItem(ItemModelGenerators generator, Item item) {
        generator.generateFlatItem(item, ModelTemplates.FLAT_HANDHELD_ITEM);
    }

    /** Potion tint on layer0 (the "head" coating), layer1 untinted. */
    private static ItemModel.Unbaked potionTinted(Identifier model) {
        return ItemModelUtils.tintedModel(model, new Potion(), new Constant(-1));
    }

    /**
     * Plain arrow; with POTION_CONTENTS -> tipped model, and if also LINGERING_STATUS=true ->
     * lingering model (replaces the old overgeared:potion_type 0/1/2 predicate).
     */
    private void upgradeArrowModel(ItemModelGenerators generator, Item item) {
        String name = itemId(item).getPath();

        Identifier baseModel = flatModel(generator, ModelLocationUtils.getModelLocation(item),
                modLoc("item/" + name), ModelTemplates.FLAT_ITEM);
        Identifier tippedModel = layeredModel(generator, modLoc("item/" + name + "_tipped"),
                modLoc("item/tipped_" + name + "_head"), modLoc("item/tipped_" + name + "_base"));
        Identifier lingeringModel = layeredModel(generator, modLoc("item/" + name + "_lingering"),
                modLoc("item/lingering_" + name + "_head"), modLoc("item/lingering_" + name + "_base"));

        ItemModel.Unbaked potionModel = ItemModelUtils.select(
                new ComponentContents<>(ModComponents.LINGERING_STATUS),
                potionTinted(tippedModel),
                ItemModelUtils.when(Boolean.TRUE, potionTinted(lingeringModel)));

        generator.itemModelOutput.accept(item, ItemModelUtils.conditional(
                ItemModelUtils.hasComponent(DataComponents.POTION_CONTENTS),
                potionModel,
                ItemModelUtils.plainModel(baseModel)));
    }

    /** Flint lingering arrow: always the lingering art; potion tint only when it carries a potion. */
    private void lingeringArrowModel(ItemModelGenerators generator, Item item) {
        String name = itemId(item).getPath();
        Identifier model = layeredModel(generator, ModelLocationUtils.getModelLocation(item),
                modLoc("item/" + name + "_head"), modLoc("item/" + name + "_base"));
        generator.itemModelOutput.accept(item, ItemModelUtils.conditional(
                ItemModelUtils.hasComponent(DataComponents.POTION_CONTENTS),
                potionTinted(model),
                ItemModelUtils.plainModel(model)));
    }

    private static Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, path);
    }
}
