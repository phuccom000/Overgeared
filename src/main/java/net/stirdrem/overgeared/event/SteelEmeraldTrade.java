package net.stirdrem.overgeared.event;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.stirdrem.overgeared.item.ModItems;

public class SteelEmeraldTrade implements VillagerTrades.ItemListing {

    private final boolean steelForEmerald;

    public SteelEmeraldTrade(boolean steelForEmerald) {
        this.steelForEmerald = steelForEmerald;
    }

    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource rand) {

        if (steelForEmerald) {
            // player gives 1 steel → get 2 emeralds
            ItemCost costA = new ItemCost(ModItems.STEEL_INGOT.get(), 1);
            ItemStack result = new ItemStack(Items.EMERALD, 2);

            // baseCostA, costB (optional), result, maxUses, xp, priceMultiplier
            return new MerchantOffer(costA, result, 12, 5, 0.05f);
        } else {
            // player gives 2 emerald → get 1 steel
            ItemCost costA = new ItemCost(Items.EMERALD, 2);
            ItemStack result = new ItemStack(ModItems.STEEL_INGOT.get(), 1);

            return new MerchantOffer(costA, result, 12, 5, 0.05f);
        }
    }
}
