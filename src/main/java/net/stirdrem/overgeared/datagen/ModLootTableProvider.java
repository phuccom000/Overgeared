package net.stirdrem.overgeared.datagen;

import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/**
 * Block loot tables, matching the original Forge mod: every block drops itself except the stone
 * anvil, which crumbles back into cobblestone.
 */
public class ModLootTableProvider extends FabricBlockLootSubProvider {
    public ModLootTableProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(dataOutput, registriesFuture);
    }

    @Override
    public void generate() {
        dropSelf(ModBlocks.STEEL_BLOCK);
        dropSelf(ModBlocks.DRAFTING_TABLE);
        dropSelf(ModBlocks.SMITHING_ANVIL);
        dropSelf(ModBlocks.TIER_A_SMITHING_ANVIL);
        dropSelf(ModBlocks.TIER_B_SMITHING_ANVIL);
        dropOther(ModBlocks.STONE_SMITHING_ANVIL, Blocks.COBBLESTONE);
        dropSelf(ModBlocks.ALLOY_FURNACE);
        dropSelf(ModBlocks.NETHER_ALLOY_FURNACE);
        dropSelf(ModBlocks.CAST_FURNACE);
    }
}
