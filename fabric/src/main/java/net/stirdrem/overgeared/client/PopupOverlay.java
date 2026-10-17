package net.stirdrem.overgeared.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.stirdrem.overgeared.config.ClientConfig;

import java.util.List;

/**
 * Draws the floating "hit rating" popup text (perfect/good/miss) that appears during forging.
 */
public class PopupOverlay {

    private static final float POPUP_DURATION_MS = 10000f;

    public static void register() {
        HudRenderCallback.EVENT.register(PopupOverlay::render);
    }

    private static void render(GuiGraphics context, float tickDelta) {
        if (!ClientConfig.POP_UP_TOGGLE.get()) return;

        List<AnvilMinigameEvents.Popup> popups = AnvilMinigameEvents.getPopups();
        if (popups.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        Font font = client.font;

        for (int i = 0; i < popups.size(); i++) {
            AnvilMinigameEvents.Popup popup = popups.get(i);

            float progress = popup.age / POPUP_DURATION_MS;
            progress = Math.min(progress, 1f);

            float alpha = 1f - progress;
            float floatUp = progress * 12f;
            float scale = 1f + (1f - progress) * 0.15f;

            int color = ((int) (alpha * 255) << 24) | 0xFFFFFF;

            int textWidth = font.width(popup.text);

            float yOffset = i * 6f;

            float popupY = screenHeight / 2f - 40 - floatUp - yOffset;

            context.pose().pushPose();
            context.pose().translate(screenWidth / 2f, popupY, 0);
            context.pose().scale(scale, scale, 1f);

            context.drawString(
                    font,
                    popup.text,
                    -textWidth / 2,
                    screenHeight / 2 - 18 - ClientConfig.MINIGAME_OVERLAY_HEIGHT.get(),
                    color,
                    false
            );

            context.pose().popPose();
        }
    }
}
