package net.stirdrem.overgeared.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.stirdrem.overgeared.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Extra items added to vanilla chest loot. NeoForge generates global loot modifiers from this list;
 * Fabric adds them as loot pools.
 */
public final class LootInjections {
    private LootInjections() {
    }

    /**
     * Adds one {@code item} to the {@code table} with probability {@code chance}.
     *
     * @param name the name of the generated loot modifier
     */
    public record Injection(String name, ResourceLocation table, Supplier<? extends Item> item, float chance) {
    }

    private static final ResourceLocation SIMPLE_DUNGEON = chest("simple_dungeon");
    private static final ResourceLocation ABANDONED_MINESHAFT = chest("abandoned_mineshaft");
    private static final ResourceLocation JUNGLE_TEMPLE_DISPENSER = chest("jungle_temple_dispenser");

    private static final List<ResourceLocation> RARE_DUNGEONS = List.of(
            chest("stronghold_corridor"),
            chest("stronghold_crossing"),
            chest("stronghold_library"),
            chest("desert_pyramid"),
            chest("shipwreck_treasure"),
            chest("woodland_mansion"),
            chest("jungle_temple"),
            chest("ancient_city"),
            chest("pillager_outpost"),
            chest("buried_treasure")
    );

    private static final List<ResourceLocation> COMMON_DUNGEONS = List.of(
            ABANDONED_MINESHAFT,
            SIMPLE_DUNGEON
    );

    public static List<Injection> all() {
        List<Injection> injections = new ArrayList<>();

        // Rare dungeons
        for (ResourceLocation dungeon : RARE_DUNGEONS) {
            String namePrefix = dungeon.getPath().replace("chests/", "");
            injections.add(new Injection("steel_ingot_from_" + namePrefix, dungeon, ModItems.STEEL_INGOT, 0.75f));
            injections.add(new Injection("diamond_upgrade_from_" + namePrefix, dungeon, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE, 0.50f));
        }

        injections.add(new Injection("iron_arrow_from_jungle_temple_dispenser", JUNGLE_TEMPLE_DISPENSER, ModItems.IRON_UPGRADE_ARROW, 0.50f));

        // Less rare dungeons
        for (ResourceLocation dungeon : COMMON_DUNGEONS) {
            String namePrefix = dungeon.getPath().replace("chests/", "");
            injections.add(new Injection("steel_ingot_from_" + namePrefix, dungeon, ModItems.STEEL_INGOT, 0.50f));
            injections.add(new Injection("steel_ingot_from_" + namePrefix + "_2", dungeon, ModItems.STEEL_INGOT, 0.35f));
            injections.add(new Injection("diamond_upgrade_from_" + namePrefix, dungeon, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE, 0.15f));
        }
        return injections;
    }

    private static ResourceLocation chest(String name) {
        return ResourceLocation.withDefaultNamespace("chests/" + name);
    }
}
