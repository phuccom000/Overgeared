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
import net.stirdrem.overgeared.loot.LootInjections;
import net.stirdrem.overgeared.loot.QualityLootModifier;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {

    public ModGlobalLootModifiersProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, OvergearedMod.MOD_ID);
    }

    @Override
    protected void start() {

        // Apply poor quality to all tools globally
        this.add("all_tools_give_quality",
                new QualityLootModifier(new LootItemCondition[]{}));

        for (LootInjections.Injection injection : LootInjections.all()) {
            this.add(injection.name(),
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(injection.table()).build(),
                            LootItemRandomChanceCondition.randomChance(injection.chance()).build()
                    }, injection.item().get()));
        }
    }
}
