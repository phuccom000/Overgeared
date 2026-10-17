package net.stirdrem.overgeared.datapack.quality_attribute;

import net.minecraft.resources.ResourceLocation;

public record QualityTarget(
        TargetType type,
        ResourceLocation id // nullable for weapon/armor
) {
    public enum TargetType {
        WEAPON, ARMOR, ITEM, ITEM_TAG, ITEM_ALL
    }
}
