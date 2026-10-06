package net.stirdrem.overgeared.loot;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.item.ModItems;

import java.util.List;

public class ModLootModifiers {

    private static final List<ResourceKey<LootTable>> OTHER_DUNGEONS = List.of(
            BuiltInLootTables.STRONGHOLD_CORRIDOR,
            BuiltInLootTables.STRONGHOLD_CROSSING,
            BuiltInLootTables.STRONGHOLD_LIBRARY,
            BuiltInLootTables.DESERT_PYRAMID,
            BuiltInLootTables.SHIPWRECK_TREASURE,
            BuiltInLootTables.WOODLAND_MANSION,
            BuiltInLootTables.JUNGLE_TEMPLE,
            BuiltInLootTables.ANCIENT_CITY,
            BuiltInLootTables.PILLAGER_OUTPOST,
            BuiltInLootTables.BURIED_TREASURE,
            // 26.x structures. Vaults roll the top-level reward table, which already pulls from the
            // nested common/rare/unique tables, so only the top-level one is listed.
            BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS,
            BuiltInLootTables.ABANDONED_CAMP_SECRET_CHEST
    );

    private static final List<ResourceKey<LootTable>> LESS_RARE_DUNGEONS = List.of(
            BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.SIMPLE_DUNGEON,
            // 26.x: regular trial chamber vaults
            BuiltInLootTables.TRIAL_CHAMBERS_REWARD
    );

    public static void register() {
        // Register the codec of the global quality function so tables containing it stay (de)serializable.
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Overgeared.id("quality"), QualityLootFunction.MAP_CODEC);

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            tableBuilder.apply(QualityLootFunction.INSTANCE);

            if (OTHER_DUNGEONS.contains(key)) {
                // Steel Ingot (75% chance)
                tableBuilder.pool(chancePool(ModItems.STEEL_INGOT, 0.75f));
                // Diamond Upgrade Template (50% chance)
                tableBuilder.pool(chancePool(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE, 0.50f));
            }

            // Jungle Temple Dispenser
            if (key.equals(BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER)) {
                tableBuilder.pool(chancePool(ModItems.IRON_UPGRADE_ARROW, 0.50f));
            }

            // Less rare dungeons
            if (LESS_RARE_DUNGEONS.contains(key)) {
                // Steel Ingot (50% chance)
                tableBuilder.pool(chancePool(ModItems.STEEL_INGOT, 0.5f));
                // Steel Ingot second entry (35% chance)
                tableBuilder.pool(chancePool(ModItems.STEEL_INGOT, 0.35f));
                // Diamond Upgrade Template (15% chance)
                tableBuilder.pool(chancePool(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE, 0.15f));
            }
        });
    }

    private static LootPool chancePool(ItemLike item, float chance) {
        return LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(item))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .build();
    }
}
