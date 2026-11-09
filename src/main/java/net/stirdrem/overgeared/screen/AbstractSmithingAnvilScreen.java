package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.Overgeared;

/**
 * The original Forge version wires in a ForgingRecipeBookComponent (the recipe-book quick-fill
 * panel) here too - dropped along with the RecipeBookMenu integration on the screen handler side,
 * since it's tied to Forge's StackedContents/SlotItemHandler system this port doesn't have.
 */
public abstract class AbstractSmithingAnvilScreen<T extends AbstractSmithingAnvilScreenHandler> extends AbstractContainerScreen<T> {
    protected Identifier TEXTURE;

    public AbstractSmithingAnvilScreen(T handler, Inventory playerInv, Component title, boolean enableBlueprintSlot) {
        super(handler, playerInv, title, 176, 166);
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = 28;
        TEXTURE = enableBlueprintSlot
                ? Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "textures/gui/smithing_anvil.png")
                : Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "textures/gui/stone_smithing_anvil.png");
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(context, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;

        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        renderProgressArrow(context, x, y);
    }

    protected void renderProgressArrow(GuiGraphicsExtractor context, int x, int y) {
        if (menu.isCrafting()) {
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 89, y + 35, 176.0F, 0.0F,
                    menu.getScaledProgress(), 17, 256, 256);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        renderHitsRemaining(context);
        renderGhostResult(context, this.leftPos, this.topPos, mouseX, mouseY);
    }

    private void renderHitsRemaining(GuiGraphicsExtractor context) {
        int remainingHits = menu.getRemainingHits();
        if (remainingHits == 0) return;

        Component hitsText = Component.translatable("gui.overgeared.remaining_hits", remainingHits);
        int x = this.leftPos;
        int y = this.topPos;
        context.text(font, hitsText, x + 89, y + 17, 0xFF404040, false);
    }

    /** Semi-transparent preview of the result, drawn like vanilla's recipe-book ghost slots. */
    private void renderGhostResult(GuiGraphicsExtractor context, int x, int y, int mouseX, int mouseY) {
        ItemStack ghostResult = menu.getGhostResult();
        if (!ghostResult.isEmpty()) {
            int itemX = x + 124;
            int itemY = y + 35;

            context.fakeItem(ghostResult, itemX, itemY);
            context.fill(itemX, itemY, itemX + 16, itemY + 16, 0x30FFFFFF);
            context.itemDecorations(this.font, ghostResult, itemX, itemY);

            if (mouseX >= itemX - 1 && mouseX < itemX + 17 && mouseY >= itemY - 1 && mouseY < itemY + 17) {
                context.setTooltipForNextFrame(this.font, ghostResult, mouseX, mouseY);
            }
        }
    }
}
