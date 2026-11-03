package net.stirdrem.overgeared.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.config.ServerConfigCommon;

/**
 * Shared quality rolling for loot (QualityLootModifier / QualityLootFunction) and
 * mob equipment drops (MobMixin), driven by the qualityWeight* server configs.
 */
public final class LootQualityHelper {

    private LootQualityHelper() {
    }

    /** Whether mob drops (loot tables and equipment) may roll a quality. */
    public static boolean mobDropsCanHaveQuality() {
        ServerConfigCommon config = Overgeared.getConfig();
        return config.ENABLE_LOOT_QUALITY() && config.ENABLE_MOB_DROP_QUALITY();
    }

    /** Same eligibility as loot: anything damageable (weapons, tools, armor...) that isn't blacklisted. */
    public static boolean isEligible(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem().canBeDepleted()
                && !stack.is(ModTags.Items.QUALITY_BLACKLIST);
    }

    /** Weighted roll using the qualityWeight* configs; POOR if all weights are zero. */
    public static ForgingQuality roll(RandomSource random) {
        ServerConfigCommon config = Overgeared.getConfig();
        ForgingQuality[] tiers = ForgingQuality.tiers();
        int[] weights = {
                Math.max(0, config.QUALITY_WEIGHT_POOR()),
                Math.max(0, config.QUALITY_WEIGHT_WELL()),
                Math.max(0, config.QUALITY_WEIGHT_EXPERT()),
                Math.max(0, config.QUALITY_WEIGHT_PERFECT()),
                Math.max(0, config.QUALITY_WEIGHT_MASTER())
        };

        int total = 0;
        for (int w : weights) total += w;
        if (total == 0) return ForgingQuality.POOR;

        int r = random.nextInt(total);
        for (int i = 0; i < tiers.length; i++) {
            r -= weights[i];
            if (r < 0) return tiers[i];
        }
        return ForgingQuality.MASTER;
    }

    public static void applyRolled(ItemStack stack, RandomSource random) {
        stack.getOrCreateTag().putString(ForgingQualityHelper.QUALITY_TAG, roll(random).getDisplayName());
    }
}
