package net.stirdrem.overgeared.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.config.ClientConfig;

/**
 * Draws the QTE bar (zones, progress, and moving arrow) during anvil forging. Registered as a
 * HudRenderCallback (Fabric's equivalent of Forge's IGuiOverlay/RegisterGuiOverlaysEvent).
 */
public class AnvilMinigameOverlay {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Overgeared.MOD_ID, "textures/gui/smithing_anvil_minigame.png");

    private static final int ARROW_WIDTH = 8;
    private static final int ARROW_HEIGHT = 16;

    public static void register() {
        HudRenderCallback.EVENT.register(AnvilMinigameOverlay::render);
    }

    private static void render(GuiGraphics context, float partialTick) {
        if (!AnvilMinigameEvents.isIsVisible()) return;

        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int imageWidth = 238;
        int imageHeight = 37;
        int textureWidth = 256;
        int textureHeight = 128;

        int x = (screenWidth - imageWidth) / 2;
        int y = (screenHeight - imageHeight) - ClientConfig.MINIGAME_OVERLAY_HEIGHT.get();

        context.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, textureWidth, textureHeight);

        int barX = x + 9;
        int barY = y + 21;
        int barWidth = 220;
        int barHeight = 10;

        int perfectZoneStart = AnvilMinigameEvents.getPerfectZoneStart();
        int perfectZoneEnd = AnvilMinigameEvents.getPerfectZoneEnd();
        int goodZoneStart = AnvilMinigameEvents.getGoodZoneStart();
        int goodZoneEnd = AnvilMinigameEvents.getGoodZoneEnd();
        float arrowPosition = AnvilMinigameEvents.getArrowPosition();

        int goodStartPx = (int) (barWidth * goodZoneStart / 100f);
        int goodEndPx = (int) (barWidth * goodZoneEnd / 100f);

        if (goodEndPx > goodStartPx) {
            context.blit(TEXTURE,
                    barX + goodStartPx, barY,
                    9, 94,
                    goodEndPx - goodStartPx, barHeight,
                    textureWidth, textureHeight);
        }

        int perfectStartPx = (int) (barWidth * perfectZoneStart / 100f);
        int perfectEndPx = (int) (barWidth * perfectZoneEnd / 100f);

        if (perfectEndPx > perfectStartPx) {
            context.blit(TEXTURE,
                    barX + perfectStartPx, barY,
                    9, 72,
                    perfectEndPx - perfectStartPx, barHeight,
                    textureWidth, textureHeight);
        }

        int progressLengthPx = (int) (222 * (1 - ((float) AnvilMinigameEvents.getHitsRemaining() / AnvilMinigameEvents.getMaxHits())));

        context.blit(TEXTURE,
                x + 8, y + 12,
                8, 62,
                progressLengthPx, 5,
                textureWidth, textureHeight);

        int arrowX = barX + (int) (barWidth * arrowPosition / 100f) - 5;
        context.blit(TEXTURE,
                arrowX, barY - 3,
                9, 41,
                ARROW_WIDTH, ARROW_HEIGHT,
                textureWidth, textureHeight);
    }
}
