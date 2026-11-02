package net.stirdrem.overgeared.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.Overgeared;

import java.util.List;

/**
 * Draws the anvil minigame bar and the hit popups. Shared by both loaders; each hooks it into its HUD
 * (Forge: IGuiOverlay, Fabric: HudRenderCallback) and passes its client-config values.
 */
public final class AnvilMinigameHud {
    private static final ResourceLocation TEXTURE = Overgeared.id("textures/gui/smithing_anvil_minigame.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 128;

    private static final int IMAGE_WIDTH = 238;
    private static final int IMAGE_HEIGHT = 37;
    private static final int BAR_OFFSET_X = 9;
    private static final int BAR_OFFSET_Y = 21;
    private static final int BAR_WIDTH = 220;
    private static final int BAR_HEIGHT = 10;
    private static final int PROGRESS_WIDTH = 222;
    private static final int ARROW_WIDTH = 8;
    private static final int ARROW_HEIGHT = 16;

    private AnvilMinigameHud() {
    }

    public static void renderMinigame(GuiGraphics guiGraphics, int screenWidth, int screenHeight,
                                      float partialTick, int overlayHeight) {
        if (!AnvilMinigameEvents.isIsVisible()) return;

        int x = (screenWidth - IMAGE_WIDTH) / 2;
        int y = screenHeight - IMAGE_HEIGHT - overlayHeight;
        int barX = x + BAR_OFFSET_X;
        int barY = y + BAR_OFFSET_Y;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Background
        blit(guiGraphics, x, y, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // Good zone, then perfect zone on top
        drawZone(guiGraphics, barX, barY, AnvilMinigameEvents.getGoodZoneStart(), AnvilMinigameEvents.getGoodZoneEnd(), 94);
        drawZone(guiGraphics, barX, barY, AnvilMinigameEvents.getPerfectZoneStart(), AnvilMinigameEvents.getPerfectZoneEnd(), 72);

        // Progress bar
        int maxHits = AnvilMinigameEvents.getMaxHits();
        if (maxHits > 0) {
            float done = 1f - (float) AnvilMinigameEvents.getHitsRemaining() / maxHits;
            int progressPx = (int) (PROGRESS_WIDTH * done);
            if (progressPx > 0) {
                blit(guiGraphics, x + 8, y + 12, 8, 62, progressPx, 5);
            }
        }

        // Arrow, interpolated between ticks so it moves smoothly at any frame rate
        float arrowPosition = AnvilMinigameEvents.getArrowPosition(partialTick);
        int arrowX = barX + (int) (BAR_WIDTH * arrowPosition / 100f) - 5;
        blit(guiGraphics, arrowX, barY - 3, 9, 41, ARROW_WIDTH, ARROW_HEIGHT);
    }

    public static void renderPopups(GuiGraphics guiGraphics, int screenWidth, int screenHeight,
                                    float partialTick, int overlayHeight) {
        List<ClientAnvilMinigameData.Popup> popups = AnvilMinigameEvents.getPopups();
        if (popups.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        float centerX = screenWidth / 2f;
        int textY = screenHeight / 2 - 18 - overlayHeight;
        // Popups age once per tick; add the partial tick so the fade/float is smooth between ticks
        float partialMs = partialTick * ClientAnvilMinigameData.MS_PER_TICK;

        for (int i = 0; i < popups.size(); i++) {
            ClientAnvilMinigameData.Popup popup = popups.get(i);

            float progress = Math.min((popup.age + partialMs) / ClientAnvilMinigameData.POPUP_DURATION_MS, 1f);
            float alpha = 1f - progress;
            int alphaByte = (int) (alpha * 255);
            // Font treats alpha < 4 as fully opaque, which would flash the popup as it fades out
            if (alphaByte < 4) continue;
            float scale = 1f + alpha * 0.15f;
            int color = (alphaByte << 24) | 0xFFFFFF;

            // Float upward over time; slight vertical offset per popup so they overlap naturally
            float popupY = screenHeight / 2f - 40 - progress * 12f - i * 6f;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(centerX, popupY, 0);
            guiGraphics.pose().scale(scale, scale, 1f);
            guiGraphics.drawString(font, popup.text, -font.width(popup.text) / 2, textY, color, false);
            guiGraphics.pose().popPose();
        }
    }

    private static void drawZone(GuiGraphics guiGraphics, int barX, int barY, int start, int end, int textureV) {
        int startPx = (int) (BAR_WIDTH * start / 100f);
        int endPx = (int) (BAR_WIDTH * end / 100f);
        if (endPx > startPx) {
            blit(guiGraphics, barX + startPx, barY, 9, textureV, endPx - startPx, BAR_HEIGHT);
        }
    }

    private static void blit(GuiGraphics guiGraphics, int x, int y, int u, int v, int width, int height) {
        guiGraphics.blit(TEXTURE, x, y, u, v, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
