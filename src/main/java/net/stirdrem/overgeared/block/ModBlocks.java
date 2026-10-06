package net.stirdrem.overgeared.block;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.custom.*;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public class ModBlocks {

    public static final Block SMITHING_ANVIL = registerBlock("smithing_anvil",
            p -> new SteelSmithingAnvil(AnvilTier.IRON, p), BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion());
    public static final Block TIER_A_SMITHING_ANVIL = registerBlock("tier_a_smithing_anvil",
            p -> new TierASmithingAnvil(AnvilTier.ABOVE_A, p), BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion());
    public static final Block TIER_B_SMITHING_ANVIL = registerBlock("tier_b_smithing_anvil",
            p -> new TierBSmithingAnvil(AnvilTier.ABOVE_B, p), BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion());
    public static final Block STONE_SMITHING_ANVIL = registerBlock("stone_anvil",
            StoneSmithingAnvil::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion());
    public static final Block STEEL_BLOCK = registerBlock("steel_block",
            Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Block DRAFTING_TABLE = registerBlock("drafting_table",
            BlueprintWorkbenchBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE));
    public static final Block ALLOY_FURNACE = registerBlock("alloy_furnace",
            AlloySmelterBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).noOcclusion().requiresCorrectToolForDrops().strength(3.5F, 6.0F).lightLevel(litBlockEmission(13)));
    public static final Block NETHER_ALLOY_FURNACE = registerBlock("nether_alloy_furnace",
            NetherAlloySmelterBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_BRICKS).noOcclusion().requiresCorrectToolForDrops().strength(3.5F, 6.0F).lightLevel(litBlockEmission(13)));
    public static final Block CAST_FURNACE = registerBlock("casting_furnace",
            CastFurnaceBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.RED_NETHER_BRICKS).noOcclusion().requiresCorrectToolForDrops().strength(3.5F, 6.0F).lightLevel(litBlockEmission(13)));

    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return state -> state.getValue(BlockStateProperties.LIT) ? lightValue : 0;
    }

    /**
     * 26.x requires the registry key on both the block and item properties before construction
     * (same pattern as vanilla Blocks.register / Items.registerBlock).
     */
    private static <T extends Block> T registerBlock(String name, Function<BlockBehaviour.Properties, T> factory,
                                                     BlockBehaviour.Properties properties) {
        Identifier id = Overgeared.id(name);
        T block = factory.apply(properties.setId(ResourceKey.create(Registries.BLOCK, id)));
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .useBlockDescriptionPrefix()));
        return block;
    }

    public static void register() {
    }
}
