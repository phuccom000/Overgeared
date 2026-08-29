package net.stirdrem.overgeared.event;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.stirdrem.overgeared.item.ModItems;

public class SteelEmeraldTrade implements TradeOffers.Factory {

    private final boolean steelForEmerald; // true = 1 steel → 2 emerald, false = 2 steel → 1 emerald

    public SteelEmeraldTrade(boolean steelForEmerald) {
        this.steelForEmerald = steelForEmerald;
    }

    @Override
    public TradeOffer create(Entity trader, Random rand) {

        if (steelForEmerald) {
            // Player gives steel → gets emeralds
            ItemStack result = new ItemStack(ModItems.STEEL_INGOT, 1);
            ItemStack cost = new ItemStack(Items.EMERALD, 2);

            return new TradeOffer(cost, ItemStack.EMPTY, result, 12, 5, 0.05f);
        } else {
            // Player gives emerald → gets steel
            ItemStack result = new ItemStack(Items.EMERALD, 1);
            ItemStack cost = new ItemStack(ModItems.STEEL_INGOT, 2);

            return new TradeOffer(cost, ItemStack.EMPTY, result, 12, 5, 0.05f);
        }
    }
}
