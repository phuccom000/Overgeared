package net.stirdrem.overgeared.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.stirdrem.overgeared.config.ClientConfig;

/** Forge HUD hook for the anvil minigame bar; drawing is shared in {@link AnvilMinigameHud}. */
@OnlyIn(Dist.CLIENT)
public class AnvilMinigameOverlay {
    public static final IGuiOverlay ANVIL_MG = (gui, guiGraphics, partialTick, screenWidth, screenHeight) ->
            AnvilMinigameHud.renderMinigame(guiGraphics, screenWidth, screenHeight, partialTick,
                    ClientConfig.MINIGAME_OVERLAY_HEIGHT.get());
}
