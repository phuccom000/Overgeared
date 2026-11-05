package net.stirdrem.overgeared.datagen.loot;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.registry.RegistryEntry;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.dropSelf(ModBlocks.STEEL_BLOCK.get());
        this.dropSelf(ModBlocks.DRAFTING_TABLE.get());
        ModBlocks.TIERED_ANVILS.values().forEach(anvil -> this.dropSelf(anvil.get()));
        this.dropOther(ModBlocks.STONE_SMITHING_ANVIL.get(), Blocks.COBBLESTONE);
        this.dropSelf(ModBlocks.ALLOY_FURNACE.get());
        this.dropSelf(ModBlocks.NETHER_ALLOY_FURNACE.get());
        this.dropSelf(ModBlocks.CAST_FURNACE.get());
    }

    protected LootTable.Builder createCopperLikeOreDrops(Block pBlock, Item item) {
        return createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().<Block>map(RegistryEntry::get)::iterator;
    }
}