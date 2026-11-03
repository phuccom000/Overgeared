package net.stirdrem.overgeared.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ForgingRecipeBookComponent;
import net.stirdrem.overgeared.config.ClientConfig;

public abstract class AbstractSmithingAnvilScreen<T extends AbstractSmithingAnvilMenu> extends AbstractContainerScreen<T> implements RecipeUpdateListener {
    private ResourceLocation TEXTURE;
    private final ForgingRecipeBookComponent recipeBookComponent = new ForgingRecipeBookComponent();
    private boolean widthTooNarrow;

    public AbstractSmithingAnvilScreen(T menu, Inventory playerInv, Component title, boolean enableBlueprintSlot) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
        TEXTURE = enableBlueprintSlot ? OvergearedMod.loc("textures/gui/smithing_anvil.png") : OvergearedMod.loc("textures/gui/stone_smithing_anvil.png");
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 28;
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        if (ClientConfig.ENABLE_ANVIL_RECIPE_BOOK.get()) {
            this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, (button) ->
            {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                button.setPosition(this.leftPos + 5, this.height / 2 - 49);
            }));
        } else {
            this.recipeBookComponent.hide();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        }
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(guiGraphics, x, y);
    }

    protected void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            guiGraphics.blit(TEXTURE, x + 89, y + 35, 176, 0, menu.getScaledProgress(), 17);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBackground(guiGraphics, mouseX, mouseY, delta);
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, delta);
        } else {
            super.render(guiGraphics, mouseX, mouseY, delta);
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, delta);
            this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, true, delta);
        }

        renderHitsRemaining(guiGraphics);
        renderGhostResult(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.recipeBookComponent.renderTooltip(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
    }

    private void renderHitsRemaining(GuiGraphics guiGraphics) {
        int remainingHits = menu.getRemainingHits();
        if (remainingHits == 0) return;

        Component hitsText = Component.translatable("gui.overgeared.remaining_hits", remainingHits);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.drawString(font, hitsText, x + 89, y + 17, 4210752, false);
    }

    private void renderGhostResult(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        // If the result slot already has an item, don't show the ghost result
        // This prevents double rendering and double tooltips
        if (menu.getResultSlot().hasItem()) {
            return;
        }

        ItemStack ghostResult = menu.getGhostResult();
        if (!ghostResult.isEmpty()) {
            int itemX = x + 124;
            int itemY = y + 35;

            guiGraphics.pose().pushPose();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F); // 50% transparency

            guiGraphics.renderFakeItem(ghostResult, itemX, itemY);
            guiGraphics.renderItemDecorations(this.font, ghostResult, itemX, itemY);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // Reset alpha
            RenderSystem.disableBlend();

            guiGraphics.pose().popPose();

            // Tooltip when hovering over ghost item
            if (mouseX >= itemX - 1 && mouseX < itemX + 17 && mouseY >= itemY - 1 && mouseY < itemY + 17) {
                guiGraphics.renderTooltip(this.font, ghostResult, mouseX, mouseY);
            }
        }
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}
