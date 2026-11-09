package net.stirdrem.overgeared.fabric;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.stirdrem.overgeared.event.ModEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NeoForge rebuilds villager trades on every server start and lets listeners edit them
 * ({@code VillagerTradesEvent} / {@code WandererTradesEvent}). This does the same with the vanilla tables:
 * the trades are snapshotted on the first start (after every mod has added its own) and rebuilt from
 * that snapshot each time, so the quality wrappers are never applied twice.
 */
final class FabricTrades {
    private static final int MAX_LEVEL = 5;

    private static Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> originalTrades;
    private static Int2ObjectMap<VillagerTrades.ItemListing[]> originalWanderingTrades;

    private FabricTrades() {
    }

    static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> apply());
    }

    private static void apply() {
        if (originalTrades == null) {
            originalTrades = new HashMap<>();
            VillagerTrades.TRADES.forEach((profession, levels) -> originalTrades.put(profession, new Int2ObjectOpenHashMap<>(levels)));
            originalWanderingTrades = new Int2ObjectOpenHashMap<>(VillagerTrades.WANDERING_TRADER_TRADES);
        }

        originalTrades.forEach((profession, original) -> {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = new Int2ObjectOpenHashMap<>();
            for (int level = 1; level <= MAX_LEVEL; level++) {
                trades.put(level, toList(original.get(level)));
            }

            ModEvents.onVillagerTrades(profession, trades);

            Int2ObjectMap<VillagerTrades.ItemListing[]> target = VillagerTrades.TRADES.get(profession);
            trades.forEach((level, list) -> target.put((int) level, list.toArray(VillagerTrades.ItemListing[]::new)));
        });

        List<VillagerTrades.ItemListing> generic = toList(originalWanderingTrades.get(1));
        List<VillagerTrades.ItemListing> rare = toList(originalWanderingTrades.get(2));
        ModEvents.onWanderingTraderTrades(generic, rare);
        VillagerTrades.WANDERING_TRADER_TRADES.put(1, generic.toArray(VillagerTrades.ItemListing[]::new));
        VillagerTrades.WANDERING_TRADER_TRADES.put(2, rare.toArray(VillagerTrades.ItemListing[]::new));
    }

    private static List<VillagerTrades.ItemListing> toList(VillagerTrades.ItemListing[] listings) {
        return listings == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(listings));
    }
}
