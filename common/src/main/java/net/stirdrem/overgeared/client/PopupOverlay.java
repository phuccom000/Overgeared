package net.stirdrem.overgeared.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ClientConfig;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

import java.util.List;

public class PopupOverlay implements LayeredDraw.Layer {

    public static final PopupOverlay INSTANCE = new PopupOverlay();
    public static final ResourceLocation ID = OvergearedMod.loc("popup");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!ClientConfig.POP_UP_TOGGLE.get()) return;

        List<AnvilMinigameEvents.Popup> popups = AnvilMinigameEvents.getPopups();
        if (popups.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        float centerX = guiGraphics.guiWidth() / 2f;
        float baseY = guiGraphics.guiHeight() / 2f + 40;
        // Popups age once per tick; add the partial tick so the fade/float is smooth between ticks
        float partialMs = deltaTracker.getGameTimeDeltaPartialTick(true) * AnvilMinigameEvents.MS_PER_TICK;

        for (int i = 0; i < popups.size(); i++) {
            AnvilMinigameEvents.Popup popup = popups.get(i);

            float progress = Math.min((popup.age + partialMs) / AnvilMinigameEvents.POPUP_DURATION_MS, 1f);
            float alpha = 1f - progress;
            int alphaByte = (int) (alpha * 255);
            // Font treats alpha < 4 as fully opaque, which would flash the popup as it fades out
            if (alphaByte < 4) continue;
            float scale = 1f + alpha * 0.15f;
            int color = (alphaByte << 24) | 0xFFFFFF;

            // Float upward over time; slight vertical offset per popup so they overlap naturally
            float popupY = baseY - progress * 12f - i * 6f;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(centerX, popupY, 0);
            guiGraphics.pose().scale(scale, scale, 1f);
            guiGraphics.drawString(font, popup.text, -font.width(popup.text) / 2, 0, color, false);
            guiGraphics.pose().popPose();
        }
    }
}
