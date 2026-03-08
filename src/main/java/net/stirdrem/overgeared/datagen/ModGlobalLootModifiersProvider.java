package net.stirdrem.overgeared.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.loot.AddItemModifier;
import net.stirdrem.overgeared.loot.QualityLootModifier;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output) {
        super(output, OvergearedMod.MOD_ID);
    }

    private static final ResourceLocation SIMPLE_DUNGEON = ResourceLocation.tryBuild("minecraft", "chests/simple_dungeon");
    private static final ResourceLocation ABANDONED_MINESHAFT = ResourceLocation.tryBuild("minecraft", "chests/abandoned_mineshaft");
    private static final ResourceLocation STRONGHOLD_CORRIDOR = ResourceLocation.tryBuild("minecraft", "chests/stronghold_corridor");
    private static final ResourceLocation STRONGHOLD_CROSSING = ResourceLocation.tryBuild("minecraft", "chests/stronghold_crossing");
    private static final ResourceLocation STRONGHOLD_LIBRARY = ResourceLocation.tryBuild("minecraft", "chests/stronghold_library");
    private static final ResourceLocation DESERT_PYRAMID = ResourceLocation.tryBuild("minecraft", "chests/desert_pyramid");
    private static final ResourceLocation JUNGLE_TEMPLE = ResourceLocation.tryBuild("minecraft", "chests/jungle_temple");
    private static final ResourceLocation JUNGLE_TEMPLE_DISPENSER = ResourceLocation.tryBuild("minecraft", "chests/jungle_temple_dispenser");
    private static final ResourceLocation SHIPWRECK_TREASURE = ResourceLocation.tryBuild("minecraft", "chests/shipwreck_treasure");
    private static final ResourceLocation WOODLAND_MANSION = ResourceLocation.tryBuild("minecraft", "chests/woodland_mansion");
    private static final ResourceLocation ANCIENT_CITY = ResourceLocation.tryBuild("minecraft", "chests/ancient_city");
    private static final ResourceLocation PILLAGER_OUTPOST = ResourceLocation.tryBuild("minecraft", "chests/pillager_outpost");
    private static final ResourceLocation BURIED_TREASURE = ResourceLocation.tryBuild("minecraft", "chests/buried_treasure");

    @Override
    protected void start() {
        add("all_tools_poor_quality", new QualityLootModifier(new LootItemCondition[]{}));

        ResourceLocation[] otherDungeons = new ResourceLocation[]{
                STRONGHOLD_CORRIDOR,
                STRONGHOLD_CROSSING,
                STRONGHOLD_LIBRARY,
                DESERT_PYRAMID,
                SHIPWRECK_TREASURE,
                WOODLAND_MANSION,
                JUNGLE_TEMPLE,
                ANCIENT_CITY,
                PILLAGER_OUTPOST,
                BURIED_TREASURE
        };

        for (ResourceLocation dungeon : otherDungeons) {
            String namePrefix = dungeon.getPath().replace("chests/", "");
            add("steel_ingot_from_" + namePrefix, new AddItemModifier(new LootItemCondition[]{
                    new LootTableIdCondition.Builder(dungeon).build(),
                    LootItemRandomChanceCondition.randomChance(0.75f).build()
            }, ModItems.STEEL_INGOT.get()));

            add("diamond_upgrade_from_" + namePrefix, new AddItemModifier(new LootItemCondition[]{
                    new LootTableIdCondition.Builder(dungeon).build(),
                    LootItemRandomChanceCondition.randomChance(0.50f).build()
            }, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()));
        }

        add("iron_arrow_from_jungle_temple_dispenser", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(JUNGLE_TEMPLE_DISPENSER).build(),
                LootItemRandomChanceCondition.randomChance(0.50f).build()
        }, ModItems.IRON_UPGRADE_ARROW.get()));

        ResourceLocation[] lessRareDungeon = new ResourceLocation[]{
                ABANDONED_MINESHAFT,
                SIMPLE_DUNGEON
        };
        for (ResourceLocation dungeon : lessRareDungeon) {

            String namePrefix = dungeon.getPath().replace("chests/", "");

            add("steel_ingot_from_" + namePrefix, new AddItemModifier(new LootItemCondition[]{
                    new LootTableIdCondition.Builder(dungeon).build(),
                    LootItemRandomChanceCondition.randomChance(0.5f).build()
            }, ModItems.STEEL_INGOT.get()));

            add("steel_ingot_from_" + namePrefix + "_2", new AddItemModifier(new LootItemCondition[]{
                    new LootTableIdCondition.Builder(dungeon).build(),
                    LootItemRandomChanceCondition.randomChance(0.35f).build()
            }, ModItems.STEEL_INGOT.get()));

            add("diamond_upgrade_from_" + namePrefix, new AddItemModifier(new LootItemCondition[]{
                    new LootTableIdCondition.Builder(dungeon).build(),
                    LootItemRandomChanceCondition.randomChance(0.15f).build()
            }, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()));
        }
    }
}