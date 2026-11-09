package net.stirdrem.overgeared.event;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.util.ForgingQualityHelper;
import net.stirdrem.overgeared.util.ItemKinds;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Random parts of trades that a data-driven villager trade can't express. Trades are plain JSON
 * ({@code data/overgeared/villager_trade}, added to the vanilla trade tags under
 * {@code data/minecraft/tags/villager_trade}); every offer a trade produces passes through
 * {@link #postProcess} (see {@link net.stirdrem.overgeared.mixin.VillagerTradeMixin}):
 * <ul>
 *     <li>smiths' tools, armor and tool parts (the mod's and vanilla's) get a random forging quality
 *     and a price for it;</li>
 *     <li>blueprints get a random quality, tool type and a price for it.</li>
 * </ul>
 */
public final class QualityWrappedTrade {
    private QualityWrappedTrade() {
    }

    public static MerchantOffer postProcess(MerchantOffer offer, @Nullable Entity trader, RandomSource random) {
        ItemStack result = offer.getResult();
        if (result.is(ModItems.BLUEPRINT.get())) {
            return applyBlueprint(offer, random);
        }
        if (trader instanceof Villager villager) {
            VillagerData data = villager.getVillagerData();
            if (isSmith(data) && !isStoneTool(result)) {
                return applyQuality(offer, random, data.level());
            }
        }
        return offer;
    }

    private static boolean isSmith(VillagerData data) {
        return data.profession().is(VillagerProfession.ARMORER)
                || data.profession().is(VillagerProfession.TOOLSMITH)
                || data.profession().is(VillagerProfession.WEAPONSMITH);
    }

    private static boolean isStoneTool(ItemStack stack) {
        return stack.is(Items.STONE_SWORD)
                || stack.is(Items.STONE_PICKAXE)
                || stack.is(Items.STONE_AXE)
                || stack.is(Items.STONE_SHOVEL)
                || stack.is(Items.STONE_HOE)
                || stack.is(Items.STONE_SPEAR);
    }

    /** Gives a tool, armor or tool part offer a random forging quality and prices it accordingly. */
    public static MerchantOffer applyQuality(MerchantOffer offer, RandomSource rand, int villagerLevel) {
        ItemStack result = offer.getResult().copy();

        // Only tools, armor, or forgeable tool heads
        if (!(ItemKinds.isTiered(result)
                || ItemKinds.isArmor(result)
                || result.is(ModTags.Items.TOOL_PARTS))) {
            return offer;
        }
        int maxUses = offer.getMaxUses();

        ForgingQuality quality = ForgingQualityHelper.rollQuality(rand, villagerLevel);
        // Masterwork = only 1 available
        if (quality == ForgingQuality.MASTER) {
            maxUses = 1;
        }
        ForgingQualityHelper.applyQuality(result, quality);

        // Tier base price times the quality multiplier
        int basePrice = ForgingQualityHelper.getBasePrice(result.getItem());
        float mult = ForgingQualityHelper.getDurabilityMultiplier(quality);
        int finalPrice = Math.max(1, Math.round(basePrice * mult));

        return new MerchantOffer(
                new ItemCost(Items.EMERALD, finalPrice),
                offer.getItemCostB(),
                result,
                maxUses,
                offer.getXp(),
                offer.getPriceMultiplier()
        );
    }

    /** Wandering trader blueprints: random quality (Expert or better) and tool type, priced by quality. */
    private static MerchantOffer applyBlueprint(MerchantOffer offer, RandomSource random) {
        ItemStack result = offer.getResult().copy();
        BlueprintData blueprintData = result.get(ModComponents.BLUEPRINT_DATA.get());
        if (blueprintData == null) {
            blueprintData = new BlueprintData(ForgingQuality.WELL.getDisplayName(), "sword", 0);
        }

        ForgingQuality quality = rollBlueprintQuality(random);
        List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
        if (!types.isEmpty()) {
            ToolType type = types.get(random.nextInt(types.size()));
            result.set(ModComponents.BLUEPRINT_DATA.get(), blueprintData
                    .withQuality(quality.getDisplayName())
                    .withUses(0)
                    .withToolType(type.getId()));
        }

        return new MerchantOffer(
                blueprintCost(quality),
                offer.getItemCostB(),
                result,
                offer.getMaxUses(),
                offer.getXp(),
                offer.getPriceMultiplier()
        );
    }

    private static ForgingQuality rollBlueprintQuality(RandomSource random) {
        int roll = random.nextInt(1000);
        if (roll < 10) return ForgingQuality.MASTER;   // 1%
        if (roll < 250) return ForgingQuality.PERFECT; // 24%
        return ForgingQuality.EXPERT;                  // 75%
    }

    private static ItemCost blueprintCost(ForgingQuality quality) {
        return switch (quality) {
            case MASTER -> new ItemCost(Items.EMERALD, 128); // 2 stacks
            case PERFECT -> new ItemCost(Items.EMERALD, 64);  // 1 stack
            case EXPERT -> new ItemCost(Items.EMERALD, 32);  // half stack
            case POOR -> new ItemCost(Items.EMERALD, 8);
            default -> new ItemCost(Items.EMERALD, 16);
        };
    }
}
