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
 * Trades are data-driven since 26.x; the mod's are JSON under data/overgeared/villager_trade. Every
 * offer a trade produces (vanilla's too) gets the random parts JSON can't express: forging quality
 * for smiths' gear and quality/tool type for blueprints. The trader is the loot context's this_entity.
 */
@Mixin(VillagerTrade.class)
public abstract class VillagerTradeMixin {
    @Inject(method = "getOffer(Lnet/minecraft/world/level/storage/loot/LootContext;)Lnet/minecraft/world/item/trading/MerchantOffer;",
            at = @At("RETURN"), cancellable = true)
    private void overgeared$postProcessOffer(LootContext lootContext, CallbackInfoReturnable<MerchantOffer> cir) {
        MerchantOffer offer = cir.getReturnValue();
        if (offer == null) return;
        MerchantOffer processed = QualityWrappedTrade.postProcess(offer,
                lootContext.getOptional(LootContextParams.THIS_ENTITY), lootContext.getRandom());
        if (processed != offer) cir.setReturnValue(processed);
    }
}
