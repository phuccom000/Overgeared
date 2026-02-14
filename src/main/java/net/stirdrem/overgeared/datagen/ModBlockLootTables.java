package net.stirdrem.overgeared.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.block.ModBlocks;
import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    protected ModBlockLootTables(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        this.dropSelf(ModBlocks.STEEL_BLOCK.get());
        this.dropSelf(ModBlocks.DRAFTING_TABLE.get());
        this.dropSelf(ModBlocks.SMITHING_ANVIL.get());
        this.dropSelf(ModBlocks.TIER_A_SMITHING_ANVIL.get());
        this.dropSelf(ModBlocks.TIER_B_SMITHING_ANVIL.get());
        this.dropOther(ModBlocks.STONE_SMITHING_ANVIL.get(), Blocks.COBBLESTONE);
        this.dropSelf(ModBlocks.ALLOY_FURNACE.get());
        this.dropSelf(ModBlocks.NETHER_ALLOY_FURNACE.get());
        this.dropSelf(ModBlocks.CASTING_FURNACE.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}