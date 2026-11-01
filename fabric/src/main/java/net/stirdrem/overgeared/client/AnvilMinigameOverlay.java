package net.stirdrem.overgeared.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.GuiGraphics;
import net.stirdrem.overgeared.config.ClientConfig;

/** Fabric HUD hook for the anvil minigame bar; drawing is shared in {@link AnvilMinigameHud}. */
public class AnvilMinigameOverlay {

    public static void register() {
        HudRenderCallback.EVENT.register(AnvilMinigameOverlay::render);
    }

    private static void render(GuiGraphics guiGraphics, float partialTick) {
        AnvilMinigameHud.renderMinigame(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight(), partialTick,
                ClientConfig.MINIGAME_OVERLAY_HEIGHT.get());
    }
}
