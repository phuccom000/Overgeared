package net.stirdrem.overgeared.event;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.util.ForgingQualityHelper;

public class ForgedItemTrade implements TradeOffers.Factory {

    private final Item item;
    private final int maxUses;
    private final int xp;
    private final int villagerLevel;

    public ForgedItemTrade(Item item, int villagerLevel, int maxUses, int xp) {
        this.item = item;
        this.villagerLevel = villagerLevel;
        this.maxUses = maxUses;
        this.xp = xp;
    }

    @Override
    public TradeOffer create(Entity trader, Random rand) {
        ItemStack result = new ItemStack(item);

        ForgingQuality quality =
                ForgingQualityHelper.rollQuality(rand, villagerLevel);

        ForgingQualityHelper.applyQuality(result, quality);

        int emeralds = ForgingQualityHelper.priceForQuality(quality);

        return new TradeOffer(
                new ItemStack(Items.EMERALD, emeralds),
                result,
                maxUses,
                xp,
                0.05f
        );
    }
}
