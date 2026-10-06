package net.stirdrem.overgeared.item;

import net.minecraft.world.item.ToolMaterial;
import net.stirdrem.overgeared.util.ModTags;

public class ModToolTiers {
    // Between iron and diamond: also mines obsidian and crying obsidian (see ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL).
    public static final ToolMaterial STEEL = new ToolMaterial(
            ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL, 500, 7.0F, 3.0F, 12, ModTags.Items.STEEL_TOOL_MATERIALS);
}
