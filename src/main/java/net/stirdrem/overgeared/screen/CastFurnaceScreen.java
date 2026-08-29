package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;

public class CastFurnaceScreen extends HandledScreen<CastFurnaceScreenHandler> {

    private static final Identifier TEXTURE =
            new Identifier(Overgeared.MOD_ID, "textures/gui/cast_furnace.png");

    public CastFurnaceScreen(CastFurnaceScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float partialTick, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        if (handler.isBurning()) {
            int flame = handler.getBurnProgress();
            context.drawTexture(TEXTURE, x + 8, y + 36 + 12 - flame,
                    176, 12 - flame, 14, flame + 1);
        }

        int progress = handler.getCookProgress();
        context.drawTexture(TEXTURE, x + 79, y + 34,
                176, 14, progress + 1, 16);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTick) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, partialTick);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
