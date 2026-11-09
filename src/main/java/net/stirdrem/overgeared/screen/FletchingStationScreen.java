package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.Overgeared;

public class FletchingStationScreen extends AbstractContainerScreen<FletchingStationScreenHandler> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "textures/gui/fletching_table.png");

    public FletchingStationScreen(FletchingStationScreenHandler handler, Inventory playerInventory, Component title) {
        super(handler, playerInventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(context, mouseX, mouseY, partialTick);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        context.text(this.font, this.title, 8, 6, 0xFF404040, false);
        context.text(this.font, this.playerInventoryTitle, 8, this.imageHeight - 94, 0xFF404040, false);
    }
}
