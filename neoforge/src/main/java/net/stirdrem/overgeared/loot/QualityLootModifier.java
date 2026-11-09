package net.stirdrem.overgeared.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.NotNull;

public class QualityLootModifier extends LootModifier {
    public static final MapCodec<QualityLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).apply(inst, QualityLootModifier::new)
    );

    public QualityLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(
            ObjectArrayList<ItemStack> generatedLoot,
            LootContext context
    ) {
        LootQuality.apply(generatedLoot, context);
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends LootModifier> codec() {
        return CODEC;
    }
}
