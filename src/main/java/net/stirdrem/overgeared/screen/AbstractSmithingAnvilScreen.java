package net.stirdrem.overgeared.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.Overgeared;

/**
 * The original Forge version wires in a ForgingRecipeBookComponent (the recipe-book quick-fill
 * panel) here too - dropped along with the RecipeBookMenu integration on the screen handler side,
 * since it's tied to Forge's StackedContents/SlotItemHandler system this port doesn't have.
 */
public abstract class AbstractSmithingAnvilScreen<T extends AbstractSmithingAnvilScreenHandler> extends AbstractContainerScreen<T> {
    protected ResourceLocation TEXTURE;

    public AbstractSmithingAnvilScreen(T handler, Inventory playerInv, Component title, boolean enableBlueprintSlot) {
        super(handler, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = 28;
        TEXTURE = enableBlueprintSlot
                ? new ResourceLocation(Overgeared.MOD_ID, "textures/gui/smithing_anvil.png")
                : new ResourceLocation(Overgeared.MOD_ID, "textures/gui/stone_smithing_anvil.png");
    }

    @Override
    protected void renderBg(GuiGraphics context, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        context.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(context, x, y);
    }

    protected void renderProgressArrow(GuiGraphics context, int x, int y) {
        if (menu.isCrafting()) {
            context.blit(TEXTURE, x + 89, y + 35, 176, 0, menu.getScaledProgress(), 17);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        renderHitsRemaining(context);
        renderGhostResult(context, this.leftPos, this.topPos, mouseX, mouseY);
        this.renderTooltip(context, mouseX, mouseY);
    }

    private void renderHitsRemaining(GuiGraphics context) {
        int remainingHits = menu.getRemainingHits();
        if (remainingHits == 0) return;

        Component hitsText = Component.translatable("gui.overgeared.remaining_hits", remainingHits);
        int x = this.leftPos;
        int y = this.topPos;
        context.drawString(font, hitsText, x + 89, y + 17, 4210752, false);
    }

    private void renderGhostResult(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
        ItemStack ghostResult = menu.getGhostResult();
        if (!ghostResult.isEmpty()) {
            int itemX = x + 124;
            int itemY = y + 35;

            context.pose().pushPose();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F); // 50% transparency

            context.renderItem(ghostResult, itemX, itemY);
            context.renderItemDecorations(this.font, ghostResult, itemX, itemY);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // Reset alpha
            RenderSystem.disableBlend();

            context.pose().popPose();

            if (mouseX >= itemX - 1 && mouseX < itemX + 17 && mouseY >= itemY - 1 && mouseY < itemY + 17) {
                context.renderTooltip(this.font, ghostResult, mouseX, mouseY);
            }
        }
    }
}
