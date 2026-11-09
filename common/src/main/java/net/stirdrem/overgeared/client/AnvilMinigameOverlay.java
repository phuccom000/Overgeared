package net.stirdrem.overgeared.client;
import net.minecraft.client.renderer.RenderPipelines;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ClientConfig;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

/** The minigame HUD; each loader registers {@link #render} as a HUD layer. */
public class AnvilMinigameOverlay {

    public static final AnvilMinigameOverlay INSTANCE = new AnvilMinigameOverlay();
    public static final Identifier ID = OvergearedMod.loc("anvil_minigame");

    private static final Identifier TEXTURE = OvergearedMod.loc("textures/gui/smithing_anvil_minigame.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 128;

    // UI dimensions
    public static final int barTotalWidth = 184;
    private static final int IMAGE_WIDTH = 238;
    private static final int IMAGE_HEIGHT = 37;
    private static final int BAR_OFFSET_X = 9;
    private static final int BAR_OFFSET_Y = 21;
    private static final int BAR_WIDTH = 220;
    private static final int BAR_HEIGHT = 10;
    private static final int PROGRESS_WIDTH = 222;
    private static final int ARROW_WIDTH = 8;
    private static final int ARROW_HEIGHT = 16;
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!AnvilMinigameEvents.isVisible()) return;

        int x = (guiGraphics.guiWidth() - IMAGE_WIDTH) / 2;
        int y = guiGraphics.guiHeight() - IMAGE_HEIGHT - ClientConfig.MINIGAME_OVERLAY_HEIGHT.get();
        int barX = x + BAR_OFFSET_X;
        int barY = y + BAR_OFFSET_Y;

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
        float arrowPosition = AnvilMinigameEvents.getArrowPosition(deltaTracker.getGameTimeDeltaPartialTick(true));
        int arrowX = barX + (int) (BAR_WIDTH * arrowPosition / 100f) - 5;
        blit(guiGraphics, arrowX, barY - 3, 9, 41, ARROW_WIDTH, ARROW_HEIGHT);
    }

    private static void drawZone(GuiGraphicsExtractor guiGraphics, int barX, int barY, int start, int end, int textureV) {
        int startPx = (int) (BAR_WIDTH * start / 100f);
        int endPx = (int) (BAR_WIDTH * end / 100f);
        if (endPx > startPx) {
            blit(guiGraphics, barX + startPx, barY, 9, textureV, endPx - startPx, BAR_HEIGHT);
        }
    }

    private static void blit(GuiGraphicsExtractor guiGraphics, int x, int y, int u, int v, int width, int height) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, u, v, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
