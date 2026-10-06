package net.stirdrem.overgeared.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public ModBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
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
                        ModBlocks.NETHER_ALLOY_FURNACE,
                        ModBlocks.CAST_FURNACE
                );

        // Wooden, like the crafting table it is made from
        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.DRAFTING_TABLE);

        /*
         * Steel tier: iron < steel < diamond. Steel can't harvest diamond-tier blocks except
         * obsidian and crying obsidian.
         */
        getOrCreateTagBuilder(ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL)
                .add(
                        Blocks.NETHERITE_BLOCK,
                        Blocks.RESPAWN_ANCHOR,
                        Blocks.ANCIENT_DEBRIS
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

        /*
         * Copper tier: stone < copper < iron (as in the original mod's needs_copper_tool).
         * Iron ore and iron blocks need at least copper: stone (and wooden) tools can't harvest
         * them, vanilla's copper tools can (copper only fails on #needs_iron_tool / diamond).
         */
        getOrCreateTagBuilder(
                BlockTags.INCORRECT_FOR_STONE_TOOL
        ).add(
                Blocks.IRON_ORE,
                Blocks.DEEPSLATE_IRON_ORE,
                Blocks.RAW_IRON_BLOCK,
                Blocks.IRON_BLOCK
        );

        getOrCreateTagBuilder(
                BlockTags.NEEDS_DIAMOND_TOOL
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
                Identifier.fromNamespaceAndPath(
                        "c",
                        "storage_blocks/steel"
                )
        );

        getOrCreateTagBuilder(STEEL_STORAGE_BLOCKS)
                .add(ModBlocks.STEEL_BLOCK);
    }



    private ModItemTagProvider.ValueTagAppender<Block> getOrCreateTagBuilder(TagKey<Block> tag) {
        return new ModItemTagProvider.ValueTagAppender<>(builder(tag), block -> block.builtInRegistryHolder().key());
    }
}
