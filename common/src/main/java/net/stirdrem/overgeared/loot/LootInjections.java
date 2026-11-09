package net.stirdrem.overgeared.loot;

import net.minecraft.resources.Identifier;
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
    public record Injection(String name, Identifier table, Supplier<? extends Item> item, float chance) {
    }

    private static final Identifier SIMPLE_DUNGEON = chest("simple_dungeon");
    private static final Identifier ABANDONED_MINESHAFT = chest("abandoned_mineshaft");
    private static final Identifier JUNGLE_TEMPLE_DISPENSER = chest("jungle_temple_dispenser");

    private static final List<Identifier> RARE_DUNGEONS = List.of(
            chest("stronghold_corridor"),
            chest("stronghold_crossing"),
            chest("stronghold_library"),
            chest("desert_pyramid"),
            chest("shipwreck_treasure"),
            chest("woodland_mansion"),
            chest("jungle_temple"),
            chest("ancient_city"),
            chest("pillager_outpost"),
            chest("buried_treasure"),
            // 26.x structures. Vaults roll the top-level reward table, which pulls from the nested
            // common/rare/unique tables, so only the top-level one is listed.
            chest("trial_chambers/reward_ominous"),
            chest("abandoned_camp_secret_chest")
    );

    private static final List<Identifier> COMMON_DUNGEONS = List.of(
            ABANDONED_MINESHAFT,
            SIMPLE_DUNGEON,
            // 26.x: regular trial chamber vaults
            chest("trial_chambers/reward")
    );

    public static List<Injection> all() {
        List<Injection> injections = new ArrayList<>();

        // Rare dungeons
        for (Identifier dungeon : RARE_DUNGEONS) {
            String namePrefix = dungeon.getPath().replace("chests/", "").replace('/', '_');
            injections.add(new Injection("steel_ingot_from_" + namePrefix, dungeon, ModItems.STEEL_INGOT, 0.75f));
            injections.add(new Injection("diamond_upgrade_from_" + namePrefix, dungeon, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE, 0.50f));
        }

        injections.add(new Injection("iron_arrow_from_jungle_temple_dispenser", JUNGLE_TEMPLE_DISPENSER, ModItems.IRON_UPGRADE_ARROW, 0.50f));

        // Less rare dungeons
        for (Identifier dungeon : COMMON_DUNGEONS) {
            String namePrefix = dungeon.getPath().replace("chests/", "").replace('/', '_');
            injections.add(new Injection("steel_ingot_from_" + namePrefix, dungeon, ModItems.STEEL_INGOT, 0.50f));
            injections.add(new Injection("steel_ingot_from_" + namePrefix + "_2", dungeon, ModItems.STEEL_INGOT, 0.35f));
            injections.add(new Injection("diamond_upgrade_from_" + namePrefix, dungeon, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE, 0.15f));
        }
        return injections;
    }

    private static Identifier chest(String name) {
        return Identifier.withDefaultNamespace("chests/" + name);
    }
}
