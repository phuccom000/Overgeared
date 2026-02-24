package net.stirdrem.overgeared.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ForgingRecipeBookComponent;

public class StoneSmithingAnvilScreen extends AbstractSmithingAnvilScreen<StoneSmithingAnvilMenu> {

    public StoneSmithingAnvilScreen(StoneSmithingAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, new ForgingRecipeBookComponent(), false);
    }
}
