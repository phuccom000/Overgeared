package net.stirdrem.overgeared.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.heateditem.HeatedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayer.class)
public class ContainerCoolingMixin {

    @Unique
    private AbstractContainerMenu overgeared$lastMenu = null;

    // Runs before sendAllDataToRemote — cools items before the client ever sees them
    @Inject(method = "initMenu", at = @At("HEAD"))
    private void onInitMenu(AbstractContainerMenu menu, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        Level level = player.level();
        if (level.isClientSide) return;
        if (overgeared$isBlacklisted(menu)) return;

        for (Slot slot : menu.slots) {
            if (HeatedItem.isHeated(slot.getItem())) {
                HeatedItem.handleCoolingContainer(slot, level, true);
            }
        }
    }

    @Inject(method = "doTick", at = @At("HEAD"))
    private void tickContainerCooling(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        AbstractContainerMenu menu = player.containerMenu;

        overgeared$lastMenu = menu;

        // No external container open — inventoryTick handles the player's own inventory
        if (menu == player.inventoryMenu) return;

        Level level = player.level();
        if (level.isClientSide) return;
        if (overgeared$isBlacklisted(menu)) return;

        for (Slot slot : menu.slots) {
            if (HeatedItem.isHeated(slot.getItem())) {
                HeatedItem.handleCoolingContainer(slot, level);
            }
        }
    }

    @Unique
    private static boolean overgeared$isBlacklisted(AbstractContainerMenu menu) {
        List<?> blacklist = ServerConfig.COOLING_BLOCK_ENTITY_BLACKLIST.get();
        if (blacklist.isEmpty()) return false;
        for (Slot slot : menu.slots) {
            if (slot.container instanceof BlockEntity be) {
                ResourceLocation beType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType());
                if (beType != null && blacklist.contains(beType.toString())) {
                    return true;
                }
                break; // only need to check one block entity slot
            }
        }
        return false;
    }
}
