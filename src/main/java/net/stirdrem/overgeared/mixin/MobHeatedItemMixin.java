package net.stirdrem.overgeared.mixin;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.heateditem.HeatedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MobHeatedItemMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide || !(entity instanceof Mob)) return;

        Level level = entity.level();

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!HeatedItem.isHeated(stack)) continue;

            if (!entity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                entity.hurt(entity.damageSources().hotFloor(), 1.0f);
            }
            HeatedItem.handleCoolingLivingEntity(stack, level, entity);
            break;
        }
    }
}
