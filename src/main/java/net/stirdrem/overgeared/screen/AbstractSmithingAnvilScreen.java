package net.stirdrem.overgeared.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;

/**
 * The original Forge version wires in a ForgingRecipeBookComponent (the recipe-book quick-fill
 * panel) here too - dropped along with the RecipeBookMenu integration on the screen handler side,
 * since it's tied to Forge's StackedContents/SlotItemHandler system this port doesn't have.
 */
public abstract class AbstractSmithingAnvilScreen<T extends AbstractSmithingAnvilScreenHandler> extends HandledScreen<T> {
    protected Identifier TEXTURE;

    public AbstractSmithingAnvilScreen(T handler, PlayerInventory playerInv, Text title, boolean enableBlueprintSlot) {
        super(handler, playerInv, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
        this.titleX = 28;
        TEXTURE = enableBlueprintSlot
                ? new Identifier(Overgeared.MOD_ID, "textures/gui/smithing_anvil.png")
                : new Identifier(Overgeared.MOD_ID, "textures/gui/stone_smithing_anvil.png");
    }

    @Override
    protected void drawBackground(DrawContext context, float partialTick, int mouseX, int mouseY) {
        int x = this.x;
        int y = this.y;

        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        renderProgressArrow(context, x, y);
    }

    protected void renderProgressArrow(DrawContext context, int x, int y) {
        if (handler.isCrafting()) {
            context.drawTexture(TEXTURE, x + 89, y + 35, 176, 0, handler.getScaledProgress(), 17);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        renderHitsRemaining(context);
        renderGhostResult(context, this.x, this.y, mouseX, mouseY);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private void renderHitsRemaining(DrawContext context) {
        int remainingHits = handler.getRemainingHits();
        if (remainingHits == 0) return;

        Text hitsText = Text.translatable("gui.overgeared.remaining_hits", remainingHits);
        int x = this.x;
        int y = this.y;
        context.drawText(textRenderer, hitsText, x + 89, y + 17, 4210752, false);
    }

    private void renderGhostResult(DrawContext context, int x, int y, int mouseX, int mouseY) {
        ItemStack ghostResult = handler.getGhostResult();
        if (!ghostResult.isEmpty()) {
            int itemX = x + 124;
            int itemY = y + 35;

            context.getMatrices().push();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F); // 50% transparency

            context.drawItem(ghostResult, itemX, itemY);
            context.drawItemInSlot(this.textRenderer, ghostResult, itemX, itemY);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // Reset alpha
            RenderSystem.disableBlend();

            context.getMatrices().pop();

            if (mouseX >= itemX - 1 && mouseX < itemX + 17 && mouseY >= itemY - 1 && mouseY < itemY + 17) {
                context.drawItemTooltip(this.textRenderer, ghostResult, mouseX, mouseY);
            }
        }
    }
}
