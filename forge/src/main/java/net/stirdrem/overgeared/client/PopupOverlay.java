package net.stirdrem.overgeared.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.stirdrem.overgeared.config.ClientConfig;

/** Forge HUD hook for the minigame hit popups; drawing is shared in {@link AnvilMinigameHud}. */
@OnlyIn(Dist.CLIENT)
public class PopupOverlay {
    public static final IGuiOverlay POPUP_OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!ClientConfig.POP_UP_TOGGLE.get()) return;
        AnvilMinigameHud.renderPopups(guiGraphics, screenWidth, screenHeight, partialTick,
                ClientConfig.MINIGAME_OVERLAY_HEIGHT.get());
    };
}
