package net.stirdrem.overgeared.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.Properties;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.custom.*;

import java.util.function.ToIntFunction;

public class ModBlocks {

    public static final Block SMITHING_ANVIL = registerBlock("smithing_anvil",
            new SteelSmithingAnvil(AnvilTier.IRON, AbstractBlock.Settings.copy(Blocks.ANVIL).nonOpaque()));
    public static final Block TIER_A_SMITHING_ANVIL = registerBlock("tier_a_smithing_anvil",
            new TierASmithingAnvil(AnvilTier.ABOVE_A, AbstractBlock.Settings.copy(Blocks.ANVIL).nonOpaque()));
    public static final Block TIER_B_SMITHING_ANVIL = registerBlock("tier_b_smithing_anvil",
            new TierBSmithingAnvil(AnvilTier.ABOVE_B, AbstractBlock.Settings.copy(Blocks.ANVIL).nonOpaque()));
    public static final Block STONE_SMITHING_ANVIL = registerBlock("stone_anvil",
            new StoneSmithingAnvil(AbstractBlock.Settings.copy(Blocks.STONE).nonOpaque()));
    public static final Block STEEL_BLOCK = registerBlock("steel_block",
            new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)));
    public static final Block DRAFTING_TABLE = registerBlock("drafting_table",
            new BlueprintWorkbenchBlock(AbstractBlock.Settings.copy(Blocks.CRAFTING_TABLE)));
    public static final Block ALLOY_FURNACE = registerBlock("alloy_furnace",
            new AlloySmelterBlock(AbstractBlock.Settings.copy(Blocks.BRICKS).nonOpaque().requiresTool().strength(3.5F, 6.0F).luminance(litBlockEmission(13))));
    public static final Block NETHER_ALLOY_FURNACE = registerBlock("nether_alloy_furnace",
            new NetherAlloySmelterBlock(AbstractBlock.Settings.copy(Blocks.NETHER_BRICKS).nonOpaque().requiresTool().strength(3.5F, 6.0F).luminance(litBlockEmission(13))));
    public static final Block CAST_FURNACE = registerBlock("casting_furnace",
            new CastFurnaceBlock(AbstractBlock.Settings.copy(Blocks.RED_NETHER_BRICKS).nonOpaque().requiresTool().strength(3.5F, 6.0F).luminance(litBlockEmission(13))));

    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return state -> state.get(Properties.LIT) ? lightValue : 0;
    }

    private static <T extends Block> T registerBlock(String name, T block) {
        Registry.register(Registries.BLOCK, Overgeared.id(name), block);
        Registry.register(Registries.ITEM, Overgeared.id(name), new BlockItem(block, new FabricItemSettings()));
        return block;
    }

    public static void register() {
    }
}
