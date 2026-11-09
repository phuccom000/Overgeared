package net.stirdrem.overgeared.event;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;

import java.util.List;

/**
 * 26.3 port: the wandering-trader blueprint trades are data-driven
 * ({@code data/overgeared/villager_trade/blueprint_common|blueprint_rare.json}); this rolls the
 * blueprint's quality / tool type and its price on every generated offer (see
 * {@link QualityWrappedTrade#postProcess}).
 */
public final class BlueprintWanderingTrade {
    private BlueprintWanderingTrade() {
    }

    public static MerchantOffer apply(MerchantOffer offer, RandomSource random) {
        ItemStack result = offer.getResult().copy();

        BlueprintQuality quality = rollQuality(random);
        BlueprintData data = BlueprintData.createDefault().withQuality(quality.getId()).withUses(0);

        // ---------- RANDOM TOOL TYPE ----------
        List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
        if (!types.isEmpty()) {
            ToolType type = types.get(random.nextInt(types.size()));
            data = data.withToolType(type.getId());
        }
        result.set(ModComponents.BLUEPRINT_DATA, data);

        // ---------- EMERALD PRICE BY QUALITY ----------
        int price = getEmeraldCostForQuality(quality);

        return new MerchantOffer(
                new ItemCost(Items.EMERALD, price),
                offer.getItemCostB(),
                result,
                offer.getMaxUses(),
                offer.getXp(),
                offer.getPriceMultiplier()
        );
    }

    private static BlueprintQuality rollQuality(RandomSource random) {
        int roll = random.nextInt(1000);

        // 0–9     → MASTER  (1%)
        // 10–249  → PERFECT (24%)
        // 250–999 → EXPERT  (75%)
        if (roll < 10) return BlueprintQuality.MASTER;
        if (roll < 250) return BlueprintQuality.PERFECT;
        return BlueprintQuality.EXPERT;
    }

    private static int getEmeraldCostForQuality(BlueprintQuality quality) {
        return switch (quality) {
            case MASTER -> 128; // 2 stacks
            case PERFECT -> 64;  // 1 stack
            case EXPERT -> 32;  // half stack
            case WELL -> 16;
            case POOR -> 8;
        };
    }
}
