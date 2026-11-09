package net.stirdrem.overgeared.datagen;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceKey;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, OvergearedMod.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
                        key(ModBlocks.STEEL_BLOCK.get()),
                        key(ModBlocks.STONE_SMITHING_ANVIL.get()),
                        key(ModBlocks.ALLOY_FURNACE.get()),
                        key(ModBlocks.NETHER_ALLOY_FURNACE.get()),
                        key(ModBlocks.CAST_FURNACE.get()));

        // Wooden, like the crafting table it is made from
        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(key(ModBlocks.DRAFTING_TABLE.get()));

        this.tag(ModTags.Blocks.SMITHING_ANVIL)
                .add(
                        key(ModBlocks.STONE_SMITHING_ANVIL.get()));
        ModBlocks.TIERED_ANVILS.values().forEach(block -> {
            this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(key(block.get()));
            this.tag(ModTags.Blocks.SMITHING_ANVIL).add(key(block.get()));
        });

        /*
         * Copper tier:
         * Stone < Copper < Iron
         */
        this.tag(ModTags.Blocks.NEEDS_COPPER_TOOL)
                .add(
                        key(Blocks.RAW_IRON_BLOCK),
                        key(Blocks.IRON_ORE),
                        key(Blocks.DEEPSLATE_IRON_ORE),
                        key(Blocks.IRON_BLOCK));

        /*
         * Steel tier:
         * Iron < Steel < Diamond
         */
        this.tag(ModTags.Blocks.NEEDS_STEEL_TOOL)
                .add(
                        key(Blocks.OBSIDIAN),
                        key(Blocks.CRYING_OBSIDIAN));

        this.tag(BlockTags.create(Identifier.parse("c:storage_blocks/steel")))
                .add(key(ModBlocks.STEEL_BLOCK.get()));

        this.tag(ModTags.Blocks.ANVIL_BASES)
                .add(key(Blocks.STONE));

        this.tag(ModTags.Blocks.IRON_ANVIL_BASES)
                .add(key(Blocks.ANVIL));

        /*
         * COPPER
         *
         * Start with everything that Stone cannot mine,
         * then remove the blocks Copper is allowed to mine.
         */
        this.tag(ModTags.Blocks.INCORRECT_FOR_COPPER_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_STONE_TOOL)
                .remove(
                        key(Blocks.RAW_IRON_BLOCK),
                        key(Blocks.IRON_ORE),
                        key(Blocks.DEEPSLATE_IRON_ORE),
                        key(Blocks.IRON_BLOCK));

        /*
         * STEEL
         *
         * Start with everything Iron cannot mine,
         * then remove the blocks Steel is allowed to mine.
         */
        this.tag(ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_IRON_TOOL)
                .remove(
                        key(Blocks.OBSIDIAN),
                        key(Blocks.CRYING_OBSIDIAN));

        /*
         * WOOD
         */
        this.tag(BlockTags.INCORRECT_FOR_WOODEN_TOOL)
                .addTag(ModTags.Blocks.NEEDS_COPPER_TOOL)
                .addTag(BlockTags.NEEDS_IRON_TOOL)
                .addTag(ModTags.Blocks.NEEDS_STEEL_TOOL)
                .addTag(BlockTags.NEEDS_DIAMOND_TOOL);

        /*
         * STONE
         *
         * Stone must no longer be able to harvest the Copper tier.
         */
        this.tag(BlockTags.INCORRECT_FOR_STONE_TOOL)
                .addTag(ModTags.Blocks.NEEDS_COPPER_TOOL)
                .addTag(ModTags.Blocks.NEEDS_STEEL_TOOL);

        /*
         * GOLD
         */
        this.tag(BlockTags.INCORRECT_FOR_GOLD_TOOL)
                .addTag(ModTags.Blocks.NEEDS_COPPER_TOOL)
                .addTag(BlockTags.NEEDS_IRON_TOOL)
                .addTag(ModTags.Blocks.NEEDS_STEEL_TOOL)
                .addTag(BlockTags.NEEDS_DIAMOND_TOOL);

        /*
         * IRON
         *
         * Iron cannot harvest Steel or Diamond-level blocks.
         */
        this.tag(BlockTags.INCORRECT_FOR_IRON_TOOL)
                .addTag(ModTags.Blocks.NEEDS_STEEL_TOOL)
                .addTag(BlockTags.NEEDS_DIAMOND_TOOL);
    }

    private static ResourceKey<Block> key(Block value) {
        return value.builtInRegistryHolder().key();
    }
}