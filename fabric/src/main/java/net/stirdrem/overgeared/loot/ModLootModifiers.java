package net.stirdrem.overgeared.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;

public class ModLootModifiers {
    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) ->
                tableBuilder.apply(QualityLootFunction.INSTANCE));
    }
}
