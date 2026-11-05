package net.stirdrem.overgeared.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.stirdrem.overgeared.registry.DeferredHolder;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, OvergearedMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.STEEL_BLOCK);

        // Tiered anvils (and their items) use the hand-made model block/<tier>_smithing_anvil
        ModBlocks.TIERED_ANVILS.forEach((tier, block) -> {
            ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/" + tier.blockName()));
            horizontalBlock(block.get(), model);
            simpleBlockItem(block.get(), model);
        });
        horizontalBlock(ModBlocks.STONE_SMITHING_ANVIL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/stone_anvil")));

        // Register item models for anvils using their block models
        simpleBlockItem(ModBlocks.STONE_SMITHING_ANVIL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/stone_anvil")));

        facingLitBlock(ModBlocks.ALLOY_FURNACE.get(), "alloy_furnace", "alloy_furnace_on");
        facingLitBlock(ModBlocks.NETHER_ALLOY_FURNACE.get(), "nether_alloy_furnace", "nether_alloy_furnace_on");
        facingLitBlock(ModBlocks.CAST_FURNACE.get(), "casting_furnace", "casting_furnace_on");

        // Drafting table has a manually-created blockstates/block model, just need the item model
        simpleBlockItem(ModBlocks.DRAFTING_TABLE.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/drafting_table")));
    }

    // Generic method to work with DeferredHolder
    private <T extends Block> void blockWithItem(DeferredHolder<Block, T> blockHolder) {
        simpleBlockWithItem(blockHolder.get(), cubeAll(blockHolder.get()));
    }

    protected void facingLitBlock(Block block, String baseModelName, String litModelName) {
        DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
        BooleanProperty LIT = BlockStateProperties.LIT;

        ModelFile baseModel = models().getExistingFile(modLoc("block/" + baseModelName));
        ModelFile litModel = models().getExistingFile(modLoc("block/" + litModelName));

        getVariantBuilder(block).forAllStates(state -> {
            Direction dir = state.getValue(FACING);
            boolean lit = state.getValue(LIT);

            return ConfiguredModel.builder()
                    .modelFile(lit ? litModel : baseModel)
                    .rotationY(((int) dir.toYRot()) % 360)
                    .build();
        });

        simpleBlockItem(block, baseModel);
    }
}