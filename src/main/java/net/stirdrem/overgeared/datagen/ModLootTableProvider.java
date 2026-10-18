package net.stirdrem.overgeared.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.stirdrem.overgeared.block.ModBlocks;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput) {
        super(dataOutput);
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

    public LootTable.Builder copperLikeOreDrops(Block drop, Item item) {
        return BlockLootSubProvider.createSilkTouchDispatchTable(drop, (LootPoolEntryContainer.Builder) this.applyExplosionDecay(drop,
                ((LootPoolSingletonContainer.Builder)
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction
                                        .setCount(UniformGenerator
                                                .between(2.0f, 5.0f))))
                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }
}
