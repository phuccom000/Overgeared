package net.stirdrem.overgeared.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.stirdrem.overgeared.registry.DeferredBlock;
import net.stirdrem.overgeared.registry.DeferredRegister;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.custom.*;
import net.stirdrem.overgeared.item.ModItems;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(OvergearedMod.MOD_ID);

    /** One smithing anvil per tier except stone, in tier order. */
    public static final Map<AnvilTier, DeferredBlock<TieredSmithingAnvil>> TIERED_ANVILS = registerTieredAnvils();

    /** The iron smithing anvil, the default tier. */
    public static final DeferredBlock<TieredSmithingAnvil> IRON_SMITHING_ANVIL = TIERED_ANVILS.get(AnvilTier.iron());

    public static final DeferredBlock<StoneSmithingAnvil> STONE_SMITHING_ANVIL = registerBlock(AnvilTier.stone().blockName(),
            () -> new StoneSmithingAnvil(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion()));

    public static final DeferredBlock<Block> STEEL_BLOCK = registerBlock("steel_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<BlueprintWorkbenchBlock> DRAFTING_TABLE = registerBlock("drafting_table",
            () -> new BlueprintWorkbenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)));

    public static final DeferredBlock<AlloySmelterBlock> ALLOY_FURNACE = registerBlock("alloy_furnace",
            () -> new AlloySmelterBlock(BlockBehaviour.Properties
                    .ofFullCopy(Blocks.BRICKS)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(3.5F, 6.0F)
                    .lightLevel(litBlockEmission())));

    public static final DeferredBlock<NetherAlloySmelterBlock> NETHER_ALLOY_FURNACE = registerBlock("nether_alloy_furnace",
            () -> new NetherAlloySmelterBlock(BlockBehaviour.Properties
                    .ofFullCopy(Blocks.NETHER_BRICKS)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(3.5F, 6.0F)
                    .lightLevel(litBlockEmission())));

    public static final DeferredBlock<CastingSmelterBlock> CAST_FURNACE = registerBlock("casting_furnace",
            () -> new CastingSmelterBlock(BlockBehaviour.Properties
                    .ofFullCopy(Blocks.RED_NETHER_BRICKS)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(3.5F, 6.0F)
                    .lightLevel(litBlockEmission())));

    private static Map<AnvilTier, DeferredBlock<TieredSmithingAnvil>> registerTieredAnvils() {
        Map<AnvilTier, DeferredBlock<TieredSmithingAnvil>> anvils = new LinkedHashMap<>();
        for (AnvilTier tier : AnvilTier.all()) {
            if (tier.isStone()) continue; // has its own block, STONE_SMITHING_ANVIL
            anvils.put(tier, registerBlock(tier.blockName(),
                    () -> new TieredSmithingAnvil(tier, BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion())));
        }
        return Collections.unmodifiableMap(anvils);
    }

    /** The smithing anvil block of a tier. */
    public static AbstractSmithingAnvil anvilFor(AnvilTier tier) {
        return tier.isStone() ? STONE_SMITHING_ANVIL.get() : TIERED_ANVILS.get(tier).get();
    }

    private static ToIntFunction<BlockState> litBlockEmission() {
        return (p_50763_) -> p_50763_.getValue(BlockStateProperties.LIT) ? 13 : 0;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register() {
        BLOCKS.register();
    }
}
