package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;

/**
 * The smithing anvil screen.
 * <p>
 * The forging recipe book is not available on 26.x yet: vanilla's recipe book was rebuilt around
 * server-sent recipe displays and registry-based categories, so it needs a dedicated port. Recipe
 * viewers (JEI) still show every forging recipe.
 */
public abstract class AbstractSmithingAnvilScreen<T extends AbstractSmithingAnvilMenu> extends AbstractContainerScreen<T> {
    private final Identifier texture;

    public AbstractSmithingAnvilScreen(T menu, Inventory playerInv, Component title, boolean enableBlueprintSlot) {
        super(menu, playerInv, title, 176, 166);
        this.inventoryLabelY = this.imageHeight - 94;
        this.texture = enableBlueprintSlot ? OvergearedMod.loc("textures/gui/smithing_anvil.png") : OvergearedMod.loc("textures/gui/stone_smithing_anvil.png");
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 28;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        renderProgressArrow(guiGraphics, x, y);
    }

    protected void renderProgressArrow(GuiGraphicsExtractor guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + 89, y + 35, 176, 0, menu.getScaledProgress(), 17, 256, 256);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        renderHitsRemaining(guiGraphics);
        renderGhostResult(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
    }

    private void renderHitsRemaining(GuiGraphicsExtractor guiGraphics) {
        int remainingHits = menu.getRemainingHits();
        if (remainingHits == 0) return;

        Component hitsText = Component.translatable("gui.overgeared.remaining_hits", remainingHits);
        guiGraphics.text(font, hitsText, this.leftPos + 89, this.topPos + 17, 0xFF404040, false);
    }

    private void renderGhostResult(GuiGraphicsExtractor guiGraphics, int x, int y, int mouseX, int mouseY) {
        // If the result slot already has an item, don't show the ghost result
        // (prevents double rendering and double tooltips)
        if (menu.getResultSlot().hasItem()) {
            return;
        }

        ItemStack ghostResult = menu.getGhostResult();
        if (!ghostResult.isEmpty()) {
            int itemX = x + 124;
            int itemY = y + 35;

            // Drawn like vanilla ghost recipe slots: the item, washed out by a translucent overlay
            guiGraphics.fakeItem(ghostResult, itemX, itemY);
            guiGraphics.fill(itemX, itemY, itemX + 16, itemY + 16, 0x30FFFFFF);
            guiGraphics.itemDecorations(this.font, ghostResult, itemX, itemY);

            if (mouseX >= itemX - 1 && mouseX < itemX + 17 && mouseY >= itemY - 1 && mouseY < itemY + 17) {
                guiGraphics.setTooltipForNextFrame(this.font, ghostResult, mouseX, mouseY);
            }
        }
    }
}
