package net.stirdrem.overgeared.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        /*
         * Pickaxe mineable
         */
        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
                        ModBlocks.STEEL_BLOCK,
                        ModBlocks.SMITHING_ANVIL,
                        ModBlocks.STONE_SMITHING_ANVIL,
                        ModBlocks.TIER_A_SMITHING_ANVIL,
                        ModBlocks.TIER_B_SMITHING_ANVIL,
                        ModBlocks.ALLOY_FURNACE,
                        ModBlocks.NETHER_ALLOY_FURNACE
                );

        /*
         * Smithing anvils
         */
        getOrCreateTagBuilder(ModTags.Blocks.SMITHING_ANVIL)
                .add(
                        ModBlocks.SMITHING_ANVIL,
                        ModBlocks.STONE_SMITHING_ANVIL,
                        ModBlocks.TIER_A_SMITHING_ANVIL,
                        ModBlocks.TIER_B_SMITHING_ANVIL
                );

        /*
         * Stone anvil bases
         */
        getOrCreateTagBuilder(ModTags.Blocks.STONE_ANVIL_BASES)
                .add(Blocks.STONE);

        /*
         * Iron anvil bases
         */
        getOrCreateTagBuilder(ModTags.Blocks.IRON_ANVIL_BASES)
                .add(Blocks.ANVIL);

        getOrCreateTagBuilder(
                MiningLevelManager.getBlockTag(2)
        ).add(
                Blocks.IRON_ORE,
                Blocks.DEEPSLATE_IRON_ORE,
                Blocks.RAW_IRON_BLOCK,
                Blocks.IRON_BLOCK
        );

        getOrCreateTagBuilder(
                MiningLevelManager.getBlockTag(3)
        ).add(
                Blocks.OBSIDIAN,
                Blocks.CRYING_OBSIDIAN,
                Blocks.NETHERITE_BLOCK,
                Blocks.ANCIENT_DEBRIS,
                Blocks.RESPAWN_ANCHOR
        );
        /*
         * Forge storage_blocks/steel equivalent
         */
        TagKey<Block> STEEL_STORAGE_BLOCKS = TagKey.create(
                Registries.BLOCK,
                new ResourceLocation(
                        "c",
                        "storage_blocks/steel"
                )
        );
        getOrCreateTagBuilder(                STEEL_STORAGE_BLOCKS        )
                .add(                ModBlocks.STEEL_BLOCK
                );
    }


}
