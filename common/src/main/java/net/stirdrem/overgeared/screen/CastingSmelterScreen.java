package net.stirdrem.overgeared.screen;
import net.minecraft.client.renderer.RenderPipelines;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.OvergearedMod;

public class CastingSmelterScreen extends AbstractContainerScreen<CastingSmelterMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(OvergearedMod.MOD_ID, "textures/gui/cast_furnace.png");

    public CastingSmelterScreen(CastingSmelterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(gfx, mouseX, mouseY, partialTick);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        // Flame
        if (menu.isBurning()) {
            int flame = menu.getBurnProgress();
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 8, y + 36 + 12 - flame, 176, 12 - flame, 14, flame + 1, 256, 256);
        }

        // Arrow
        int progress = menu.getCookProgress();
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 79, y + 34, 176, 14, progress + 1, 16, 256, 256);
    }

}
