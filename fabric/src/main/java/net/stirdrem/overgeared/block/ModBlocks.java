package net.stirdrem.overgeared.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.custom.*;

import java.util.function.ToIntFunction;

public class ModBlocks {

    public static final Block SMITHING_ANVIL = registerBlock("smithing_anvil",
            new SteelSmithingAnvil(AnvilTier.IRON, BlockBehaviour.Properties.copy(Blocks.ANVIL).noOcclusion()));
    public static final Block TIER_A_SMITHING_ANVIL = registerBlock("tier_a_smithing_anvil",
            new TierASmithingAnvil(AnvilTier.ABOVE_A, BlockBehaviour.Properties.copy(Blocks.ANVIL).noOcclusion()));
    public static final Block TIER_B_SMITHING_ANVIL = registerBlock("tier_b_smithing_anvil",
            new TierBSmithingAnvil(AnvilTier.ABOVE_B, BlockBehaviour.Properties.copy(Blocks.ANVIL).noOcclusion()));
    public static final Block STONE_SMITHING_ANVIL = registerBlock("stone_anvil",
            new StoneSmithingAnvil(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion()));
    public static final Block STEEL_BLOCK = registerBlock("steel_block",
            new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final Block DRAFTING_TABLE = registerBlock("drafting_table",
            new BlueprintWorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE)));
    public static final Block ALLOY_FURNACE = registerBlock("alloy_furnace",
            new AlloySmelterBlock(BlockBehaviour.Properties.copy(Blocks.BRICKS).noOcclusion().requiresCorrectToolForDrops().strength(3.5F, 6.0F).lightLevel(litBlockEmission(13))));
    public static final Block NETHER_ALLOY_FURNACE = registerBlock("nether_alloy_furnace",
            new NetherAlloySmelterBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_BRICKS).noOcclusion().requiresCorrectToolForDrops().strength(3.5F, 6.0F).lightLevel(litBlockEmission(13))));
    public static final Block CAST_FURNACE = registerBlock("casting_furnace",
            new CastFurnaceBlock(BlockBehaviour.Properties.copy(Blocks.RED_NETHER_BRICKS).noOcclusion().requiresCorrectToolForDrops().strength(3.5F, 6.0F).lightLevel(litBlockEmission(13))));

    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return state -> state.getValue(BlockStateProperties.LIT) ? lightValue : 0;
    }

    private static <T extends Block> T registerBlock(String name, T block) {
        Registry.register(BuiltInRegistries.BLOCK, Overgeared.id(name), block);
        Registry.register(BuiltInRegistries.ITEM, Overgeared.id(name), new BlockItem(block, new FabricItemSettings()));
        return block;
    }

    public static void register() {
    }
}
