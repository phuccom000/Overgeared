package net.stirdrem.overgeared.datapack.quality_attribute;

import net.minecraft.resources.Identifier;

public record QualityTarget(
        TargetType type,
        Identifier id // nullable for weapon/armor
) {
    public enum TargetType {
        WEAPON, ARMOR, ITEM, ITEM_TAG, ITEM_ALL
    }
}
