package net.stirdrem.overgeared.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.heateditem.HeatedItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryCoolingMixin {

    @Shadow @Final public Player player;

    @Unique
    private boolean overgeared$hasHeatedItems = false;

    // Flag when heated items enter the inventory
    @Inject(method = "setItem", at = @At("HEAD"))
    private void onSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        if (HeatedItem.isHeated(stack)) {
            overgeared$hasHeatedItems = true;
        }
    }

    // Inventory.tick() runs every player tick — scan only when flagged
    @Inject(method = "tick", at = @At("TAIL"))
    private void onInventoryTick(CallbackInfo ci) {
        if (!overgeared$hasHeatedItems) return;

        Level level = player.level();
        if (level.isClientSide) return;

        Inventory inv = (Inventory) (Object) this;
        boolean found = false;
        boolean damaged = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (!HeatedItem.isHeated(stack)) continue;

            found = true;

            // Damage + tongs only once per tick
            if (!damaged) {
                HeatedItem.tickInventory(stack, level, player);
                damaged = true;
            }

            HeatedItem.handleCoolingLivingEntity(stack, level, player);
        }

        overgeared$hasHeatedItems = found;
    }
}
