package net.stirdrem.overgeared.loot;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.util.ModTags;

/**
 * Applied globally to every loot table via LootTableEvents.MODIFY (see ModLootModifiers) -
 * Fabric has no direct equivalent of Forge's global loot modifiers, but applying a LootFunction
 * to every table's builder runs it against every stack that table generates, which is the same
 * per-stack post-process semantics as the original QualityLootModifier.
 */
public class QualityLootFunction implements LootItemFunction {
    public static final QualityLootFunction INSTANCE = new QualityLootFunction();

    private static final LootItemFunctionType TYPE = new LootItemFunctionType(new Serializer<>() {
        @Override
        public void serialize(com.google.gson.JsonObject json, LootItemFunction object, com.google.gson.JsonSerializationContext context) {
        }

        @Override
        public LootItemFunction deserialize(com.google.gson.JsonObject json, com.google.gson.JsonDeserializationContext context) {
            return INSTANCE;
        }
    });

    @Override
    public ItemStack apply(ItemStack generated, LootContext context) {
        if (!Overgeared.getConfig().ENABLE_LOOT_QUALITY()) return generated;
        if (!isEligibleItem(generated)) return generated;

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
            generated.getOrCreateTag().putString("ForgingQuality", ForgingQuality.POOR.getDisplayName());
            return generated;
        }

        RandomSource random = context.getRandom();
        int r = random.nextInt(total);
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

        generated.getOrCreateTag().putString("ForgingQuality", chosen.getDisplayName());
        return generated;
    }

    private static boolean isEligibleItem(ItemStack stack) {
        Item item = stack.getItem();

        if (!item.canBeDepleted()) return false;

        return !stack.is(ModTags.Items.QUALITY_BLACKLIST);
    }

    @Override
    public LootItemFunctionType getType() {
        return TYPE;
    }
}
