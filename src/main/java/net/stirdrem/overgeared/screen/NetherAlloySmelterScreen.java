package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.Overgeared;

public class NetherAlloySmelterScreen extends AbstractContainerScreen<NetherAlloySmelterScreenHandler> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Overgeared.MOD_ID, "textures/gui/nether_alloy_furnace.png");

    public NetherAlloySmelterScreen(NetherAlloySmelterScreenHandler handler, Inventory playerInventory, Component title) {
        super(handler, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics context, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        context.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.isLit()) {
            int litHeight = this.menu.getLitProgress();
            context.blit(TEXTURE, x + 8, y + 36 + 13 - litHeight,
                    176, 13 - litHeight, 14, litHeight + 1);
        }

        int progress = this.menu.getCookProgress();
        context.blit(TEXTURE, x + 89, y + 34, 176, 14, progress + 1, 16);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        int titleWidth = this.font.width(this.title);
        int titleX = (this.imageWidth - titleWidth) / 2;
        context.drawString(this.font, this.title, titleX, this.titleLabelY, 4210752, false);

        context.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.imageHeight - 94, 4210752, false);
    }
}
