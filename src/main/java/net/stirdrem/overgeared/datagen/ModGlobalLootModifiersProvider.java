package net.stirdrem.overgeared.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.loot.AddItemModifier;
import net.stirdrem.overgeared.loot.QualityLootModifier;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {

    public ModGlobalLootModifiersProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, OvergearedMod.MOD_ID);
    }

    // Vanilla loot tables
    private static final ResourceLocation SIMPLE_DUNGEON = ResourceLocation.withDefaultNamespace("chests/simple_dungeon");
    private static final ResourceLocation ABANDONED_MINESHAFT = ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft");
    private static final ResourceLocation STRONGHOLD_CORRIDOR = ResourceLocation.withDefaultNamespace("chests/stronghold_corridor");
    private static final ResourceLocation STRONGHOLD_CROSSING = ResourceLocation.withDefaultNamespace("chests/stronghold_crossing");
    private static final ResourceLocation STRONGHOLD_LIBRARY = ResourceLocation.withDefaultNamespace("chests/stronghold_library");
    private static final ResourceLocation DESERT_PYRAMID = ResourceLocation.withDefaultNamespace("chests/desert_pyramid");
    private static final ResourceLocation JUNGLE_TEMPLE = ResourceLocation.withDefaultNamespace("chests/jungle_temple");
    private static final ResourceLocation SHIPWRECK_TREASURE = ResourceLocation.withDefaultNamespace("chests/shipwreck_treasure");
    private static final ResourceLocation WOODLAND_MANSION = ResourceLocation.withDefaultNamespace("chests/woodland_mansion");

    @Override
    protected void start() {

        // Apply poor quality to all tools globally
        this.add("all_tools_give_quality",
                new QualityLootModifier(new LootItemCondition[]{}));

        // Rare dungeons
        ResourceLocation[] rareDungeons = new ResourceLocation[]{
                STRONGHOLD_CORRIDOR,
                STRONGHOLD_CROSSING,
                STRONGHOLD_LIBRARY,
                DESERT_PYRAMID,
                SHIPWRECK_TREASURE,
                WOODLAND_MANSION,
                JUNGLE_TEMPLE
        };

        for (ResourceLocation dungeon : rareDungeons) {
            String namePrefix = dungeon.getPath().replace("chests/", "");

            // Steel ingot
            this.add("steel_ingot_from_" + namePrefix,
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(dungeon).build(),
                            LootItemRandomChanceCondition.randomChance(0.50f).build()
                    }, ModItems.STEEL_INGOT.get()));

            // Diamond upgrade
            this.add("diamond_upgrade_from_" + namePrefix,
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(dungeon).build(),
                            LootItemRandomChanceCondition.randomChance(0.25f).build()
                    }, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()));
        }

        // Less rare dungeons
        ResourceLocation[] commonDungeons = new ResourceLocation[]{
                ABANDONED_MINESHAFT,
                SIMPLE_DUNGEON
        };

        for (ResourceLocation dungeon : commonDungeons) {
            String namePrefix = dungeon.getPath().replace("chests/", "");

            // Steel ingot (higher chance)
            this.add("steel_ingot_from_" + namePrefix,
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(dungeon).build(),
                            LootItemRandomChanceCondition.randomChance(0.50f).build()
                    }, ModItems.STEEL_INGOT.get()));

            // Steel ingot (second roll)
            this.add("steel_ingot_from_" + namePrefix + "_2",
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(dungeon).build(),
                            LootItemRandomChanceCondition.randomChance(0.35f).build()
                    }, ModItems.STEEL_INGOT.get()));

            // Diamond upgrade
            this.add("diamond_upgrade_from_" + namePrefix,
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(dungeon).build(),
                            LootItemRandomChanceCondition.randomChance(0.15f).build()
                    }, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()));
        }
    }
}
