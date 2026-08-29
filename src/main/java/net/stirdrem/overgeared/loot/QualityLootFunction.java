package net.stirdrem.overgeared.loot;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.math.random.Random;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;

/**
 * Applied globally to every loot table via LootTableEvents.MODIFY (see ModLootModifiers) -
 * Fabric has no direct equivalent of Forge's global loot modifiers, but applying a LootFunction
 * to every table's builder runs it against every stack that table generates, which is the same
 * per-stack post-process semantics as the original QualityLootModifier.
 */
public class QualityLootFunction implements LootFunction {
    public static final QualityLootFunction INSTANCE = new QualityLootFunction();

    private static final LootFunctionType TYPE = new LootFunctionType(new JsonSerializer<>() {
        @Override
        public void toJson(com.google.gson.JsonObject json, LootFunction object, com.google.gson.JsonSerializationContext context) {
        }

        @Override
        public LootFunction fromJson(com.google.gson.JsonObject json, com.google.gson.JsonDeserializationContext context) {
            return INSTANCE;
        }
    });

    @Override
    public ItemStack apply(ItemStack generated, LootContext context) {
        if (!ServerConfig.ENABLE_LOOT_QUALITY.get()) return generated;
        if (!isEligibleItem(generated)) return generated;

        int wPoor = ServerConfig.QUALITY_WEIGHT_POOR.get();
        int wWell = ServerConfig.QUALITY_WEIGHT_WELL.get();
        int wExpert = ServerConfig.QUALITY_WEIGHT_EXPERT.get();
        int wPerfect = ServerConfig.QUALITY_WEIGHT_PERFECT.get();
        int wMaster = ServerConfig.QUALITY_WEIGHT_MASTER.get();

        int total = 0;
        if (wPoor > 0) total += wPoor;
        if (wWell > 0) total += wWell;
        if (wExpert > 0) total += wExpert;
        if (wPerfect > 0) total += wPerfect;
        if (wMaster > 0) total += wMaster;

        if (total == 0) {
            generated.getOrCreateNbt().putString("ForgingQuality", ForgingQuality.POOR.getDisplayName());
            return generated;
        }

        Random random = context.getRandom();
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

        generated.getOrCreateNbt().putString("ForgingQuality", chosen.getDisplayName());
        return generated;
    }

    private static boolean isEligibleItem(ItemStack stack) {
        Item item = stack.getItem();

        if (!item.isDamageable()) return false;

        return !stack.isIn(ModTags.Items.QUALITY_BLACKLIST);
    }

    @Override
    public LootFunctionType getType() {
        return TYPE;
    }
}
