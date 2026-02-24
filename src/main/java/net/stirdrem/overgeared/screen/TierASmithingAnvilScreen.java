package net.stirdrem.overgeared.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ForgingRecipeBookComponent;
import net.stirdrem.overgeared.config.ServerConfig;

public class TierASmithingAnvilScreen extends AbstractSmithingAnvilScreen<TierASmithingAnvilMenu> {

    public TierASmithingAnvilScreen(TierASmithingAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, new ForgingRecipeBookComponent(), ServerConfig.ENABLE_BLUEPRINT_FORGING.get());

    }
}
