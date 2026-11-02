package net.stirdrem.overgeared.item;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.platform.Services;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;

public class ModToolTiers {
    // Steel sits between iron and diamond, copper between stone and iron
    public static final Tier STEEL = Services.PLATFORM.createToolTier("steel_ingot",
            2, 500, 7.0F, 3.0F, 12,
            ModTags.Blocks.NEEDS_STEEL_TOOL, () -> Ingredient.of(ModItems.STEEL_INGOT.get()),
            List.of(Tiers.IRON), List.of(Tiers.DIAMOND));

    public static final Tier COPPER = Services.PLATFORM.createToolTier("copper_ingot",
            1, 190, 5.0F, 1.0F, 12,
            ModTags.Blocks.NEEDS_COPPER_TOOL, () -> Ingredient.of(Items.COPPER_INGOT),
            List.of(Tiers.STONE), List.of(Tiers.IRON));
}
