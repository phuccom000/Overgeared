package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.client.ForgingRecipeBookComponent;

public class SteelSmithingAnvilScreen extends AbstractSmithingAnvilScreen<SteelSmithingAnvilMenu> {

    public SteelSmithingAnvilScreen(SteelSmithingAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, new ForgingRecipeBookComponent());
    }
}
