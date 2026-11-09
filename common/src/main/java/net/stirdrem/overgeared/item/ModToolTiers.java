package net.stirdrem.overgeared.item;

import net.minecraft.world.item.ToolMaterial;
import net.stirdrem.overgeared.util.ModTags;

public final class ModToolTiers {
    // Copper tools are vanilla's since 26.x (ToolMaterial.COPPER).

    public static final ToolMaterial STEEL = new ToolMaterial(
            ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL, 500, 7.0f, 3.0f, 12, ModTags.Items.STEEL_TOOL_MATERIALS);

    private ModToolTiers() {
    }
}
