package net.stirdrem.overgeared.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.config.ClientConfig;

/**
 * Draws the QTE bar (zones, progress, and moving arrow) during anvil forging. Registered as a
 * Fabric HUD element (Fabric's equivalent of Forge's IGuiOverlay/RegisterGuiOverlaysEvent).
 */
public class AnvilMinigameOverlay {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "textures/gui/smithing_anvil_minigame.png");

    private static final int ARROW_WIDTH = 8;
    private static final int ARROW_HEIGHT = 16;

    public static void register() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Overgeared.id("anvil_minigame"), AnvilMinigameOverlay::render);
    }

    private static void render(GuiGraphicsExtractor context, DeltaTracker deltaTracker) {
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

        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, textureWidth, textureHeight);

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
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                    barX + goodStartPx, barY,
                    9, 94,
                    goodEndPx - goodStartPx, barHeight,
                    textureWidth, textureHeight);
        }

        int perfectStartPx = (int) (barWidth * perfectZoneStart / 100f);
        int perfectEndPx = (int) (barWidth * perfectZoneEnd / 100f);

        if (perfectEndPx > perfectStartPx) {
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                    barX + perfectStartPx, barY,
                    9, 72,
                    perfectEndPx - perfectStartPx, barHeight,
                    textureWidth, textureHeight);
        }

        int progressLengthPx = (int) (222 * (1 - ((float) AnvilMinigameEvents.getHitsRemaining() / AnvilMinigameEvents.getMaxHits())));

        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                x + 8, y + 12,
                8, 62,
                progressLengthPx, 5,
                textureWidth, textureHeight);

        int arrowX = barX + (int) (barWidth * arrowPosition / 100f) - 5;
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                arrowX, barY - 3,
                9, 41,
                ARROW_WIDTH, ARROW_HEIGHT,
                textureWidth, textureHeight);
    }
}
