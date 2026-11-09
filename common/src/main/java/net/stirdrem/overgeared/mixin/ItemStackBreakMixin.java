package net.stirdrem.overgeared.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.util.QualityBreak;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Quality break system. Hooks the entity overload of {@code hurtAndBreak} (used by players and mobs
 * for tools, weapons and armor), as the 1.20.1 hook did. Dispensers (shears, flint and steel) damage
 * items through the ServerLevel overload directly and break normally.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackBreakMixin {
    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("HEAD"), cancellable = true)
    private void overgeared$qualityBasedBreak(int amount, LivingEntity owner, EquipmentSlot slot, CallbackInfo ci) {
        if (owner.level() instanceof ServerLevel level
                && QualityBreak.preventBreak((ItemStack) (Object) this, amount, level, owner, slot)) {
            ci.cancel();
        }
    }
}
