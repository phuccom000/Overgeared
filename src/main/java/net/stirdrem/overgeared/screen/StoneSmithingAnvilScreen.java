package net.stirdrem.overgeared.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class StoneSmithingAnvilScreen extends AbstractSmithingAnvilScreen<StoneSmithingAnvilScreenHandler> {
    public StoneSmithingAnvilScreen(StoneSmithingAnvilScreenHandler handler, PlayerInventory playerInv, Text title) {
        super(handler, playerInv, title, false);
    }
}
