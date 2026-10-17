package net.stirdrem.overgeared.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StoneSmithingAnvilScreen extends AbstractSmithingAnvilScreen<StoneSmithingAnvilScreenHandler> {
    public StoneSmithingAnvilScreen(StoneSmithingAnvilScreenHandler handler, Inventory playerInv, Component title) {
        super(handler, playerInv, title, false);
    }
}
