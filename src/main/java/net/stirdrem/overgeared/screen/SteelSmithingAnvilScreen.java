package net.stirdrem.overgeared.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.stirdrem.overgeared.config.ServerConfig;

public class SteelSmithingAnvilScreen extends AbstractSmithingAnvilScreen<SteelSmithingAnvilScreenHandler> {
    public SteelSmithingAnvilScreen(SteelSmithingAnvilScreenHandler handler, PlayerInventory playerInv, Text title) {
        super(handler, playerInv, title, ServerConfig.ENABLE_BLUEPRINT_FORGING.get());
    }
}
