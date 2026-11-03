package net.stirdrem.overgeared.loot;

import net.stirdrem.overgeared.Overgeared;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;
import net.stirdrem.overgeared.util.LootQualityHelper;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.NotNull;

public class QualityLootModifier extends LootModifier {
    public static final Codec<QualityLootModifier> CODEC = RecordCodecBuilder.create(inst ->
            codecStart(inst).apply(inst, QualityLootModifier::new));

    public QualityLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!Overgeared.getConfig().ENABLE_LOOT_QUALITY()) return generatedLoot;
        if (!Overgeared.getConfig().ENABLE_MOB_DROP_QUALITY() && isMobDrop(context)) return generatedLoot;

        for (ItemStack stack : generatedLoot) {
            if (!isEligibleItem(stack)) continue;
            LootQualityHelper.applyRolled(stack, context.getRandom());
        }

        return generatedLoot;
    }

    /** Mob death loot tables are the only ones that carry a damage source. */
    private static boolean isMobDrop(LootContext context) {
        return context.hasParam(LootContextParams.DAMAGE_SOURCE);
    }

    private static boolean isEligibleItem(ItemStack stack) {
        Item item = stack.getItem();

        if (!item.isDamageable(stack)) return false;

        return !stack.is(ModTags.Items.QUALITY_BLACKLIST);
    }

    @Override
    public Codec<? extends LootModifier> codec() {
        return CODEC;
    }
}
