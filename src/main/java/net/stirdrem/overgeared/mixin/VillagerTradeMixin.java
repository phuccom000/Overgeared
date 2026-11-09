package net.stirdrem.overgeared.mixin;

import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.stirdrem.overgeared.event.QualityWrappedTrade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.3 port: replaces the Fabric TradeOfferHelper wrapping of the 1.20 version. Every offer a
 * (data-driven) VillagerTrade produces is post-processed: smith trades get a rolled forging quality
 * and quality-based price, wandering-trader blueprints get a rolled blueprint quality/tool type.
 */
@Mixin(VillagerTrade.class)
public abstract class VillagerTradeMixin {

    @Inject(method = "getOffer(Lnet/minecraft/world/level/storage/loot/LootContext;)Lnet/minecraft/world/item/trading/MerchantOffer;",
            at = @At("RETURN"), cancellable = true)
    private void overgeared$applyQuality(LootContext lootContext, CallbackInfoReturnable<MerchantOffer> cir) {
        MerchantOffer offer = cir.getReturnValue();
        if (offer == null) return;
        MerchantOffer processed = QualityWrappedTrade.postProcess(offer,
                lootContext.getOptional(LootContextParams.THIS_ENTITY), lootContext.getRandom());
        if (processed != offer) cir.setReturnValue(processed);
    }
}
