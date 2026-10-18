package net.stirdrem.overgeared.loot;

import net.stirdrem.overgeared.Overgeared;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;
import net.stirdrem.overgeared.ForgingQuality;
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

        int wPoor = Overgeared.getConfig().QUALITY_WEIGHT_POOR();
        int wWell = Overgeared.getConfig().QUALITY_WEIGHT_WELL();
        int wExpert = Overgeared.getConfig().QUALITY_WEIGHT_EXPERT();
        int wPerfect = Overgeared.getConfig().QUALITY_WEIGHT_PERFECT();
        int wMaster = Overgeared.getConfig().QUALITY_WEIGHT_MASTER();

        int total = 0;
        if (wPoor > 0) total += wPoor;
        if (wWell > 0) total += wWell;
        if (wExpert > 0) total += wExpert;
        if (wPerfect > 0) total += wPerfect;
        if (wMaster > 0) total += wMaster;

        if (total == 0) {
            for (ItemStack stack : generatedLoot) {
                if (isEligibleItem(stack)) {
                    stack.getOrCreateTag().putString("ForgingQuality", ForgingQuality.POOR.getDisplayName());
                }
            }
            return generatedLoot;
        }

        for (ItemStack stack : generatedLoot) {
            if (!isEligibleItem(stack)) continue;

            int r = context.getRandom().nextInt(total);
            ForgingQuality chosen;
            int accum = 0;
            accum += wPoor;
            if (r < accum) {
                chosen = ForgingQuality.POOR;
            } else {
                accum += wWell;
                if (r < accum) {
                    chosen = ForgingQuality.WELL;
                } else {
                    accum += wExpert;
                    if (r < accum) {
                        chosen = ForgingQuality.EXPERT;
                    } else {
                        accum += wPerfect;
                        if (r < accum) {
                            chosen = ForgingQuality.PERFECT;
                        } else {
                            chosen = ForgingQuality.MASTER;
                        }
                    }
                }
            }

            stack.getOrCreateTag().putString("ForgingQuality", chosen.getDisplayName());
        }

        return generatedLoot;
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
