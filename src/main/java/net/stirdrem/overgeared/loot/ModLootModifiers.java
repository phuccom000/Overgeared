package net.stirdrem.overgeared.loot;


import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.function.Supplier;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, OvergearedMod.MOD_ID);

    public static final Supplier<MapCodec<AddItemModifier>> ADD_ITEM =
            GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("add_item", () -> AddItemModifier.CODEC);

    public static final Supplier<MapCodec<QualityLootModifier>> QUALITY_LOOT_MODIFIER =
            GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("quality_loot", () -> QualityLootModifier.CODEC);


    public static void register(IEventBus eventBus) {
        GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }
}