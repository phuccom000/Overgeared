package net.stirdrem.overgeared.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, OvergearedMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
                        ModBlocks.STEEL_BLOCK.get(),
                        ModBlocks.SMITHING_ANVIL.get(),
                        ModBlocks.TIER_A_SMITHING_ANVIL.get(),
                        ModBlocks.TIER_B_SMITHING_ANVIL.get(),
                        ModBlocks.STONE_SMITHING_ANVIL.get(),
                        ModBlocks.ALLOY_FURNACE.get(),
                        ModBlocks.NETHER_ALLOY_FURNACE.get(),
                        ModBlocks.CASTING_FURNACE.get()
                );

        this.tag(ModTags.Blocks.SMITHING_ANVIL)
                .add(
                        ModBlocks.SMITHING_ANVIL.get(),
                        ModBlocks.STONE_SMITHING_ANVIL.get(),
                        ModBlocks.TIER_A_SMITHING_ANVIL.get(),
                        ModBlocks.TIER_B_SMITHING_ANVIL.get()
                );

        /*
         * Copper tier:
         * Stone < Copper < Iron
         */
        this.tag(ModTags.Blocks.NEEDS_COPPER_TOOL)
                .add(
                        Blocks.RAW_IRON_BLOCK,
                        Blocks.IRON_ORE,
                        Blocks.DEEPSLATE_IRON_ORE,
                        Blocks.IRON_BLOCK
                );

        /*
         * Steel tier:
         * Iron < Steel < Diamond
         */
        this.tag(ModTags.Blocks.NEEDS_STEEL_TOOL)
                .add(
                        Blocks.OBSIDIAN,
                        Blocks.CRYING_OBSIDIAN
                );

        this.tag(BlockTags.create(ResourceLocation.parse("c:storage_blocks/steel")))
                .add(ModBlocks.STEEL_BLOCK.get());

        this.tag(ModTags.Blocks.ANVIL_BASES)
                .add(Blocks.STONE);

        this.tag(ModTags.Blocks.IRON_ANVIL_BASES)
                .add(Blocks.ANVIL);

        /*
         * COPPER
         *
         * Start with everything that Stone cannot mine,
         * then remove the blocks Copper is allowed to mine.
         */
        this.tag(ModTags.Blocks.INCORRECT_FOR_COPPER_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_STONE_TOOL)
                .remove(
                        Blocks.RAW_IRON_BLOCK,
                        Blocks.IRON_ORE,
                        Blocks.DEEPSLATE_IRON_ORE,
                        Blocks.IRON_BLOCK
                );

        /*
         * STEEL
         *
         * Start with everything Iron cannot mine,
         * then remove the blocks Steel is allowed to mine.
         */
        this.tag(ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_IRON_TOOL)
                .remove(
                        Blocks.OBSIDIAN,
                        Blocks.CRYING_OBSIDIAN
                );

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
}