package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;

public class AlloySmelterScreen extends HandledScreen<AlloySmelterScreenHandler> {
    private static final Identifier TEXTURE =
            new Identifier(Overgeared.MOD_ID, "textures/gui/brick_alloy_furnace.png");

    public AlloySmelterScreen(AlloySmelterScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void drawBackground(DrawContext context, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        if (this.handler.isLit()) {
            int litHeight = this.handler.getLitProgress();
            context.drawTexture(TEXTURE, x + 8, y + 36 + 13 - litHeight,
                    176, 13 - litHeight, 14, litHeight + 1);
        }

        int progress = this.handler.getCookProgress();
        context.drawTexture(TEXTURE, x + 85, y + 34, 176, 14, progress + 1, 16);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int titleWidth = this.textRenderer.getWidth(this.title);
        int titleX = (this.backgroundWidth - titleWidth) / 2;
        context.drawText(this.textRenderer, this.title, titleX, this.titleY, 4210752, false);

        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.backgroundHeight - 94, 4210752, false);
    }
}
