package net.stirdrem.overgeared.datagen;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class DataGenerators {
    /** The "data" run (clientData) generates everything: client assets and server data. */
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(ModModelProvider::new);
        gatherServer(event);
    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        gatherServer(event);
    }

    private static void gatherServer(GatherDataEvent event) {
        // Recipes (with their advancements) and loot tables are datapack registry entries since 1.21.2
        event.getDefaultPackGenerator().createReloadableRegistryObjects(new RegistrySetBuilder()
                .add(RecipeProvider.asBootstrap(ModRecipeProvider::new))
                .add(Registries.LOOT_TABLE, new LootTableProvider(Set.of(),
                        List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)))),
                // minecraft too: some recipes deliberately replace vanilla ones (e.g. minecraft:iron_pickaxe)
                Set.of(OvergearedMod.MOD_ID, "minecraft"));

        event.createBlockAndItemTags(ModBlockTagGenerator::new, (output, lookup, blockTags) -> new ModItemTagGenerator(output, lookup));
        event.createProvider(ModGlobalLootModifiersProvider::new);
    }
}
