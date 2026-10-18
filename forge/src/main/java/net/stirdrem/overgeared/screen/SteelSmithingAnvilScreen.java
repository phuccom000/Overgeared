package net.stirdrem.overgeared.screen;

import net.stirdrem.overgeared.Overgeared;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.client.ForgingRecipeBookComponent;

public class SteelSmithingAnvilScreen extends AbstractSmithingAnvilScreen<SteelSmithingAnvilMenu> {

    public SteelSmithingAnvilScreen(SteelSmithingAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, new ForgingRecipeBookComponent(), Overgeared.getConfig().ENABLE_BLUEPRINT_FORGING());
    }
}
