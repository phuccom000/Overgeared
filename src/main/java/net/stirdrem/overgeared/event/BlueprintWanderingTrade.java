package net.stirdrem.overgeared.event;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlueprintWanderingTrade implements TradeOffers.Factory {

    private final ItemStack blueprintItem;
    private final int maxUses;
    private final int traderXp;

    public BlueprintWanderingTrade(ItemStack blueprintItem, int maxUses, int traderXp) {
        this.blueprintItem = blueprintItem;
        this.maxUses = maxUses;
        this.traderXp = traderXp;
    }

    @Override
    public @Nullable TradeOffer create(Entity entity, Random random) {

        ItemStack result = blueprintItem.copy();
        NbtCompound tag = result.getOrCreateNbt();

        BlueprintQuality quality = rollQuality(random);

        tag.putString("Quality", quality.name());
        tag.putInt("Uses", 0);

        // ---------- RANDOM TOOL TYPE ----------
        List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
        if (!types.isEmpty()) {
            ToolType type = types.get(random.nextInt(types.size()));
            tag.putString("ToolType", type.getId());
        }

        // ---------- EMERALD PRICE BY QUALITY ----------
        ItemStack emeraldCost = getEmeraldCostForQuality(quality);

        return new TradeOffer(
                emeraldCost,          // Cost A (emeralds)
                ItemStack.EMPTY,      // Cost B
                result,               // Result
                maxUses,
                traderXp,
                0.05F
        );
    }

    private BlueprintQuality rollQuality(Random random) {
        int roll = random.nextInt(1000);

        // 0–9     → MASTER  (1%)
        // 10–249  → PERFECT (24%)
        // 250–999 → EXPERT  (75%)

        if (roll < 10) return BlueprintQuality.MASTER;
        if (roll < 250) return BlueprintQuality.PERFECT;
        return BlueprintQuality.EXPERT;
    }

    private ItemStack getEmeraldCostForQuality(BlueprintQuality quality) {
        return switch (quality) {
            case MASTER -> new ItemStack(Items.EMERALD, 128); // 2 stacks
            case PERFECT -> new ItemStack(Items.EMERALD, 64);  // 1 stack
            case EXPERT -> new ItemStack(Items.EMERALD, 32);  // half stack
            case WELL -> new ItemStack(Items.EMERALD, 16);
            case POOR -> new ItemStack(Items.EMERALD, 8);
        };
    }
}
