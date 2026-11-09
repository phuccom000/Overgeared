package net.stirdrem.overgeared.loot;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;

/**
 * Gives generated loot a random forging quality. Used by NeoForge's global loot modifier and
 * by the Fabric loot table hook.
 */
public final class LootQuality {
    private LootQuality() {
    }

    public static void apply(List<ItemStack> generatedLoot, LootContext context) {
        if (!ServerConfig.ENABLE_LOOT_QUALITY.get()) return;
        if (!ServerConfig.ENABLE_MOB_DROP_QUALITY.get() && isMobDrop(context)) return;
        apply(generatedLoot, context.getRandom());
    }

    /**
     * Entity death loot is the only loot context that carries a damage source.
     */
    private static boolean isMobDrop(LootContext context) {
        return context.hasParameter(LootContextParams.DAMAGE_SOURCE);
    }

    private static void apply(List<ItemStack> generatedLoot, RandomSource random) {

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

        // if everything disabled, default to poor
        if (total == 0) {
            for (ItemStack stack : generatedLoot) {
                if (isEligibleItem(stack)) {
                    stack.set(ModComponents.FORGING_QUALITY.get(), ForgingQuality.POOR);
                }
            }
            return;
        }

        for (ItemStack stack : generatedLoot) {
            if (!isEligibleItem(stack)) continue;

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

            stack.set(ModComponents.FORGING_QUALITY.get(), chosen);
        }

        return;
    }

    public static boolean isEligibleItem(ItemStack stack) {
        if (!stack.has(DataComponents.MAX_DAMAGE)) return false;

        return !stack.is(ModTags.Items.QUALITY_BLACKLIST);
    }
}
