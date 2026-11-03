package net.stirdrem.overgeared.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.util.ForgingQualityHelper;
import net.stirdrem.overgeared.util.LootQualityHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Mob equipment (held weapons/tools, worn armor) drops through Mob#dropCustomDeathLoot rather
 * than a loot table, so the loot modifier/function never sees it. Roll a quality here with the
 * same rules, gated by enableLootQuality + enableMobDropQuality.
 */
@Mixin(Mob.class)
public abstract class MobMixin {

    @ModifyArg(
            method = "dropCustomDeathLoot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;spawnAtLocation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"
            )
    )
    private ItemStack overgeared$rollEquipmentQuality(ItemStack stack) {
        Mob self = (Mob) (Object) this;
        if (self.level().isClientSide()) return stack;
        if (!LootQualityHelper.mobDropsCanHaveQuality()) return stack;
        if (!LootQualityHelper.isEligible(stack)) return stack;
        // Keep any quality the item already had (e.g. a player's forged gear the mob picked up)
        if (stack.hasTag() && stack.getTag().contains(ForgingQualityHelper.QUALITY_TAG)) return stack;

        LootQualityHelper.applyRolled(stack, self.getRandom());
        return stack;
    }
}
