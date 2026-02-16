package net.stirdrem.overgeared.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ForgingRecipeBookComponent;

public class StoneSmithingAnvilScreen extends AbstractSmithingAnvilScreen<StoneSmithingAnvilMenu> {

    public StoneSmithingAnvilScreen(StoneSmithingAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, new ForgingRecipeBookComponent());
        TEXTURE = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/stone_smithing_anvil.png");
    }
}
