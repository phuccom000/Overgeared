package net.stirdrem.overgeared.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;

/**
 * Applied globally to every loot table via LootTableEvents.MODIFY (see ModLootModifiers) -
 * Fabric has no direct equivalent of Forge's global loot modifiers, but applying a LootFunction
 * to every table's builder runs it against every stack that table generates, which is the same
 * per-stack post-process semantics as the original QualityLootModifier.
 * Its codec is registered as {@code overgeared:quality} in BuiltInRegistries.LOOT_FUNCTION_TYPE.
 */
public class QualityLootFunction implements LootItemFunction {
    public static final QualityLootFunction INSTANCE = new QualityLootFunction();
    public static final MapCodec<QualityLootFunction> MAP_CODEC = MapCodec.unit(() -> INSTANCE);

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
            generated.set(ModComponents.FORGING_QUALITY, ForgingQuality.POOR);
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

        generated.set(ModComponents.FORGING_QUALITY, chosen);
        return generated;
    }

    private static boolean isEligibleItem(ItemStack stack) {
        if (!stack.isDamageableItem()) return false;

        return !stack.is(ModTags.Items.QUALITY_BLACKLIST);
    }

    @Override
    public MapCodec<QualityLootFunction> codec() {
        return MAP_CODEC;
    }
}
