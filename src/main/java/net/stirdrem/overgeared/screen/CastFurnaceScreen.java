package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.Overgeared;

public class CastFurnaceScreen extends AbstractContainerScreen<CastFurnaceScreenHandler> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Overgeared.MOD_ID, "textures/gui/cast_furnace.png");

    public CastFurnaceScreen(CastFurnaceScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics context, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        context.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        if (menu.isBurning()) {
            int flame = menu.getBurnProgress();
            context.blit(TEXTURE, x + 8, y + 36 + 12 - flame,
                    176, 12 - flame, 14, flame + 1);
        }

        int progress = menu.getCookProgress();
        context.blit(TEXTURE, x + 79, y + 34,
                176, 14, progress + 1, 16);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, partialTick);
        renderTooltip(context, mouseX, mouseY);
    }
}
