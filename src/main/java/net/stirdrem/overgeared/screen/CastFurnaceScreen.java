package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.Overgeared;

public class CastFurnaceScreen extends AbstractContainerScreen<CastFurnaceScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "textures/gui/cast_furnace.png");

    public CastFurnaceScreen(CastFurnaceScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(context, mouseX, mouseY, partialTick);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        if (menu.isBurning()) {
            int flame = menu.getBurnProgress();
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 8, y + 36 + 12 - flame,
                    176.0F, 12 - flame, 14, flame + 1, 256, 256);
        }

        int progress = menu.getCookProgress();
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 79, y + 34,
                176.0F, 14.0F, progress + 1, 16, 256, 256);
    }
}
