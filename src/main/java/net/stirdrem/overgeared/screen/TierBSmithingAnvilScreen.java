package net.stirdrem.overgeared.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.config.ServerConfig;

public class TierBSmithingAnvilScreen extends AbstractSmithingAnvilScreen<TierBSmithingAnvilScreenHandler> {
    public TierBSmithingAnvilScreen(TierBSmithingAnvilScreenHandler handler, Inventory playerInv, Component title) {
        super(handler, playerInv, title, ServerConfig.ENABLE_BLUEPRINT_FORGING.get());
    }
}
