package net.stirdrem.overgeared.compat.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

final class JeiUtils {
    private JeiUtils() {
    }

    /** Registry access of the current client level, or null outside a world. */
    static @Nullable HolderLookup.Provider registries() {
        var level = Minecraft.getInstance().level;
        return level != null ? level.registryAccess() : null;
    }

    /** Draws "N XP" right-aligned to {@code rightEdge}, without a trailing ".0" for whole numbers. */
    static void drawExperience(GuiGraphics guiGraphics, float exp, int rightEdge, int y) {
        String text = exp == (int) exp ? (int) exp + " XP" : String.format("%.1f XP", exp);
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, text, rightEdge - font.width(text), y, 0xFFFFFFFF, true);
    }
}
