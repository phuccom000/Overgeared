package net.stirdrem.overgeared.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.networking.packet.KnappingChipC2SPacket;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.sounds.SoundEvents.STONE_BREAK;

public class RockKnappingScreen extends AbstractContainerScreen<RockKnappingMenu> {
    private static final ResourceLocation TEXTURE = OvergearedMod.loc("textures/gui/rock_knapping_gui.png");
    private static final ResourceLocation CHIPPED_TEXTURE = OvergearedMod.loc("textures/gui/blank.png");
    private static final ResourceLocation DEFAULT_UNCHIPPED = ResourceLocation.tryBuild("minecraft", "textures/block/stone.png");

    private static final int GRID_ORIGIN_X = 32;
    private static final int GRID_ORIGIN_Y = 19;
    private static final int SLOT_SIZE = 16;

    public RockKnappingScreen(RockKnappingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        addKnappingButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (menu.isKnappingFinished()) {
            this.clearWidgets();
        }
    }

    private void addKnappingButtons() {
        this.clearWidgets();

        if (menu.isKnappingFinished()) return;

        boolean hasResult = !menu.getSlot(9).getItem().isEmpty();
        boolean resultCollected = menu.isResultCollected();

        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = this.leftPos + GRID_ORIGIN_X + col * SLOT_SIZE;
            int y = this.topPos + GRID_ORIGIN_Y + row * SLOT_SIZE;

            final int index = i;
            boolean isChipped = menu.isChipped(i);

            // Determine texture for this specific button
            WidgetSprites buttonSprites = getWidgetSprites(isChipped, resultCollected);

            ImageButton button = new KnappingImageButton(x, y, SLOT_SIZE, SLOT_SIZE, buttonSprites,
                    btn -> {
                        if ((!hasResult || (!resultCollected && hasResult)) && !isChipped) {
                            menu.setChip(index);
                            if (!resultCollected) {
                                PacketDistributor.sendToServer(new KnappingChipC2SPacket(index));
                            }
                            btn.active = false;
                        }
                    }
            );

            // Set button to inactive if already chipped
            button.active = !isChipped;

            this.addRenderableWidget(button);
        }
    }

    private @NotNull WidgetSprites getWidgetSprites(boolean isChipped, boolean resultCollected) {
        ResourceLocation currentTexture;
        if (isChipped || resultCollected) {
            currentTexture = CHIPPED_TEXTURE;
        } else {
            // Get the current unchipped texture from menu
            currentTexture = menu.getUnchippedTexture() != null ? menu.getUnchippedTexture() : DEFAULT_UNCHIPPED;
        }

        // Create WidgetSprites for this button
        return new WidgetSprites(
                currentTexture,  // enabled, not hovered
                CHIPPED_TEXTURE,  // enabled, hovered
                currentTexture, // disabled, not hovered
                CHIPPED_TEXTURE  // disabled, hovered
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);
    }

    private class KnappingImageButton extends ImageButton {
        public KnappingImageButton(int x, int y, int width, int height, WidgetSprites sprites, OnPress onPress) {
            super(x, y, width, height, sprites, onPress);
        }

        @Override
        public void playDownSound(SoundManager pHandler) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(STONE_BREAK, 1.0F, 1.0F);
            }
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation resourceLocation = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
            guiGraphics.blit(resourceLocation, this.getX(), this.getY(), 0, 0, width, height, width, height);
        }
    }
}