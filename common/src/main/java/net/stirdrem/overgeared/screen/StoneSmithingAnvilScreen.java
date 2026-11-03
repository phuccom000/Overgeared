package net.stirdrem.overgeared.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.OvergearedMod;

public class StoneSmithingAnvilScreen extends AbstractSmithingAnvilScreen<StoneSmithingAnvilMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OvergearedMod.MOD_ID, "textures/gui/stone_smithing_anvil.png");

    public StoneSmithingAnvilScreen(StoneSmithingAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, false);
    }
}
