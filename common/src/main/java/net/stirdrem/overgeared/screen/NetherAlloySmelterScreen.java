package net.stirdrem.overgeared.screen;
import net.minecraft.client.renderer.RenderPipelines;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.OvergearedMod;

public class NetherAlloySmelterScreen extends AbstractContainerScreen<NetherAlloySmelterMenu> {
    private static final Identifier TEXTURE =
            OvergearedMod.loc("textures/gui/nether_alloy_furnace.png");

    public NetherAlloySmelterScreen(NetherAlloySmelterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // --- Background ---
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        // --- Flame (fuel burn animation) ---
        if (this.menu.isLit()) {
            int litHeight = this.menu.getLitProgress();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 8, y + 36 + 13 - litHeight, 176, 13 - litHeight, 14, litHeight + 1, 256, 256);
        }

        // --- Progress arrow ---
        int progress = this.menu.getCookProgress();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 89, y + 34, 176, 14, progress + 1, 16, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // Center the title
        int titleWidth = this.font.width(this.title);
        int titleX = (this.imageWidth - titleWidth) / 2;
        guiGraphics.text(this.font, this.title, titleX, this.titleLabelY, 0xFF404040, false);

        // Player inventory title stays at the default position
        guiGraphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.imageHeight - 94, 0xFF404040, false);
    }
}
