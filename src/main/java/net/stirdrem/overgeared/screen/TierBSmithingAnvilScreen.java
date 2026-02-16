package net.stirdrem.overgeared.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ForgingRecipeBookComponent;
import net.stirdrem.overgeared.config.ServerConfig;

public class TierBSmithingAnvilScreen extends AbstractSmithingAnvilScreen<TierBSmithingAnvilMenu> {

    public TierBSmithingAnvilScreen(TierBSmithingAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, new ForgingRecipeBookComponent());
        if (!ServerConfig.ENABLE_BLUEPRINT_FORGING.get())
            TEXTURE = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/stone_smithing_anvil.png");
    }
}
