package net.stirdrem.overgeared.datagen;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.core.Holder;
import java.util.Optional;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
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
                new QualityLootModifier(Optional.empty(), 0));

        for (LootInjections.Injection injection : LootInjections.all()) {
            this.add(injection.name(),
                    new AddItemModifier(Optional.of(Holder.direct(AllOfCondition.allOf(
                            new LootTableIdCondition.Builder(injection.table()),
                            LootItemRandomChanceCondition.randomChance(injection.chance())
                    ).build())), 0, injection.item().get()));
        }
    }
}
