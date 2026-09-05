package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.Overgeared;

public class FletchingStationScreen extends AbstractContainerScreen<FletchingStationScreenHandler> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Overgeared.MOD_ID, "textures/gui/fletching_table.png");

    public FletchingStationScreen(FletchingStationScreenHandler handler, Inventory playerInventory, Component title) {
        super(handler, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics context, float partialTick, int mouseX, int mouseY) {
        context.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        context.drawString(this.font, this.title, 8, 6, 0x404040, false);
        context.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 94, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, partialTick);
        this.renderTooltip(context, mouseX, mouseY);
    }
}
