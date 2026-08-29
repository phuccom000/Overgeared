package net.stirdrem.overgeared.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

public class RockKnappingMenuProvider implements NamedScreenHandlerFactory {
    @Override
    public Text getDisplayName() {
        return Text.translatable("gui.overgeared.rock_knapping");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        // The screen handler constructor will handle checking if player has knappable rocks
        return new RockKnappingScreenHandler(syncId, inv, player.getWorld().getRecipeManager());
    }
}
