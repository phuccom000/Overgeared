package net.stirdrem.overgeared.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.config.ServerConfig;

public class SteelSmithingAnvilScreen extends AbstractSmithingAnvilScreen<SteelSmithingAnvilMenu> {

    public SteelSmithingAnvilScreen(SteelSmithingAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, ServerConfig.ENABLE_BLUEPRINT_FORGING.get());
    }
}
