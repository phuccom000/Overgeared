package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.Overgeared;

public class AlloySmelterScreen extends AbstractContainerScreen<AlloySmelterScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "textures/gui/brick_alloy_furnace.png");

    public AlloySmelterScreen(AlloySmelterScreenHandler handler, Inventory playerInventory, Component title) {
        super(handler, playerInventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(context, mouseX, mouseY, partialTick);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        if (this.menu.isLit()) {
            int litHeight = this.menu.getLitProgress();
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 8, y + 36 + 13 - litHeight,
                    176.0F, 13 - litHeight, 14, litHeight + 1, 256, 256);
        }

        int progress = this.menu.getCookProgress();
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 85, y + 34, 176.0F, 14.0F, progress + 1, 16, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int titleWidth = this.font.width(this.title);
        int titleX = (this.imageWidth - titleWidth) / 2;
        context.text(this.font, this.title, titleX, this.titleLabelY, 0xFF404040, false);

        context.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.imageHeight - 94, 0xFF404040, false);
    }
}
