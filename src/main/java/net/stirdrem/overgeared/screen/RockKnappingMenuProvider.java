package net.stirdrem.overgeared.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class RockKnappingMenuProvider implements MenuProvider {
    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.rock_knapping");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        // The screen handler constructor will handle checking if player has knappable rocks
        return new RockKnappingScreenHandler(syncId, inv, player.level().getRecipeManager());
    }
}
