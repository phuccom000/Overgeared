package net.stirdrem.overgeared.datagen;

import net.stirdrem.overgeared.registry.RegistryEntry;
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
                        ModBlocks.STEEL_BLOCK.get(),
                        ModBlocks.STONE_SMITHING_ANVIL.get(),
                        ModBlocks.ALLOY_FURNACE.get(),
                        ModBlocks.NETHER_ALLOY_FURNACE.get()
                )
                .add(ModBlocks.TIERED_ANVILS.values().stream().map(RegistryEntry::get).toArray(Block[]::new));

        /*
         * Smithing anvils
         */
        getOrCreateTagBuilder(ModTags.Blocks.SMITHING_ANVIL)
                .add(ModBlocks.STONE_SMITHING_ANVIL.get())
                .add(ModBlocks.TIERED_ANVILS.values().stream().map(RegistryEntry::get).toArray(Block[]::new));

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

        // Copper and steel tools mine these despite their level (DiggerItemMixin); the level tags keep weaker tiers out
        getOrCreateTagBuilder(ModTags.Blocks.NEEDS_COPPER_TOOL)
                .add(
                        Blocks.IRON_ORE,
                        Blocks.DEEPSLATE_IRON_ORE,
                        Blocks.RAW_IRON_BLOCK,
                        Blocks.IRON_BLOCK
                );

        getOrCreateTagBuilder(ModTags.Blocks.NEEDS_STEEL_TOOL)
                .add(
                        Blocks.OBSIDIAN,
                        Blocks.CRYING_OBSIDIAN
                );

        getOrCreateTagBuilder(MiningLevelManager.getBlockTag(2))
                .addTag(ModTags.Blocks.NEEDS_COPPER_TOOL);

        getOrCreateTagBuilder(MiningLevelManager.getBlockTag(3))
                .addTag(ModTags.Blocks.NEEDS_STEEL_TOOL)
                .add(
                        Blocks.NETHERITE_BLOCK,
                        Blocks.ANCIENT_DEBRIS,
                        Blocks.RESPAWN_ANCHOR
                );
        // Fabric 1.20.1 conventional (flat) name
        getOrCreateTagBuilder(TagKey.create(Registries.BLOCK, new ResourceLocation("c", "steel_blocks")))
                .add(ModBlocks.STEEL_BLOCK.get());
    }


}
