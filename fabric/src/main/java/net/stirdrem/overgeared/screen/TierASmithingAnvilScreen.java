package net.stirdrem.overgeared.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.Overgeared;

public class TierASmithingAnvilScreen extends AbstractSmithingAnvilScreen<TierASmithingAnvilScreenHandler> {
    public TierASmithingAnvilScreen(TierASmithingAnvilScreenHandler handler, Inventory playerInv, Component title) {
        super(handler, playerInv, title, Overgeared.getConfig().ENABLE_BLUEPRINT_FORGING());
    }
}
