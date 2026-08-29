package net.stirdrem.overgeared.event;

import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.util.ForgingQualityHelper;
import net.stirdrem.overgeared.util.ModTags;

public class QualityWrappedTrade implements TradeOffers.Factory {

    private final TradeOffers.Factory original;
    private final int villagerLevel;

    public QualityWrappedTrade(TradeOffers.Factory original, int villagerLevel) {
        this.original = original;
        this.villagerLevel = villagerLevel;
    }

    @Override
    public TradeOffer create(Entity trader, Random rand) {
        TradeOffer offer = original.create(trader, rand);
        if (offer == null) return null;

        ItemStack result = offer.getSellItem().copy();

        // Only tools, armor, or forgeable tool heads
        if (!(result.getItem() instanceof ToolItem
                || result.getItem() instanceof ArmorItem
                || result.isIn(ModTags.Items.TOOL_PARTS))) {
            return offer;
        }
        int maxUses = offer.getMaxUses();


        ForgingQuality quality =
                ForgingQualityHelper.rollQuality(rand, villagerLevel);
        // Masterwork = only 1 available
        if (quality == ForgingQuality.MASTER) {
            maxUses = 1;
        }
        ForgingQualityHelper.applyQuality(result, quality);

        // Tier base price
        int basePrice = ForgingQualityHelper.getBasePrice(result.getItem());

        // Quality multiplier
        float mult = ForgingQualityHelper.getQualityMultiplier(quality);

        int finalPrice = Math.max(1, Math.round(basePrice * mult));

        ItemStack emeraldCost = new ItemStack(Items.EMERALD, finalPrice);

        return new TradeOffer(
                emeraldCost,
                offer.getSecondBuyItem(),
                result,
                maxUses,
                offer.getMerchantExperience(),
                offer.getPriceMultiplier()
        );
    }

}
