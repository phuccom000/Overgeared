package net.stirdrem.overgeared.event;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerDataHolder;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ForgingQualityHelper;
import net.stirdrem.overgeared.util.ModTags;
import org.jspecify.annotations.Nullable;

/**
 * 26.3 port: villager trades are data-driven ({@code VillagerTrade} registry + {@code TradeSet}s
 * fed by {@code villager_trade} tags), and Fabric's TradeOfferHelper is gone. The mod's own trades
 * live in {@code data/overgeared/villager_trade/} and are added to the vanilla trade-set tags in
 * {@code data/minecraft/tags/villager_trade/}. The old per-listing wrappers (QualityWrappedTrade,
 * BlueprintWanderingTrade) are applied to every generated offer by
 * {@link net.stirdrem.overgeared.mixin.VillagerTradeMixin} through {@link #postProcess}.
 */
public final class QualityWrappedTrade {
    private QualityWrappedTrade() {
    }

    /** Called with every offer a {@code VillagerTrade} produces. Returns the (possibly replaced) offer. */
    public static @Nullable MerchantOffer postProcess(@Nullable MerchantOffer offer, @Nullable Entity trader, RandomSource random) {
        if (offer == null || trader == null) return offer;

        if (trader instanceof VillagerDataHolder villager) {
            Holder<VillagerProfession> profession = villager.getVillagerData().profession();
            if (profession.is(VillagerProfession.WEAPONSMITH)
                    || profession.is(VillagerProfession.TOOLSMITH)
                    || profession.is(VillagerProfession.ARMORER)) {
                return wrap(offer, villager.getVillagerData().level(), random);
            }
        } else if (trader instanceof WanderingTrader && offer.getResult().is(ModItems.BLUEPRINT)) {
            return BlueprintWanderingTrade.apply(offer, random);
        }
        return offer;
    }

    /** Port of the 1.20 QualityWrappedTrade#getOffer. */
    public static MerchantOffer wrap(MerchantOffer offer, int villagerLevel, RandomSource rand) {
        ItemStack result = offer.getResult().copy();

        if (isStoneTool(result)) return offer;

        // Only tools, armor, or forgeable tool heads
        if (!(QualityAttributeReloadListener.isWeaponItem(result.getItem())
                || QualityAttributeReloadListener.isArmorItem(result.getItem())
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

        // Tier base price
        int basePrice = ForgingQualityHelper.getBasePrice(result.getItem());
        // Quality multiplier
        float mult = ForgingQualityHelper.getQualityMultiplier(quality);
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

    private static boolean isStoneTool(ItemStack stack) {
        return stack.is(Items.STONE_SWORD)
                || stack.is(Items.STONE_PICKAXE)
                || stack.is(Items.STONE_AXE)
                || stack.is(Items.STONE_SHOVEL)
                || stack.is(Items.STONE_SPEAR)
                || stack.is(Items.STONE_HOE);
    }
}
