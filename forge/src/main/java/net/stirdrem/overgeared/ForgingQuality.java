package net.stirdrem.overgeared;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;

public enum ForgingQuality {
    POOR("poor"),
    WELL("well"),
    EXPERT("expert"),
    PERFECT("perfect"),
    MASTER("master"),
    NONE("none");

    private final String displayName;

    ForgingQuality(String displayName) {
        this.displayName = displayName;
    }

    public static ForgingQuality fromString(String quality) {
        for (ForgingQuality q : values()) {
            if (q.displayName.equalsIgnoreCase(quality)) return q;
        }
        return POOR; // fallback
    }

    public String getDisplayName() {
        return displayName;
    }

    public ForgingQuality getLowerQuality() {
        // NONE should never downgrade to MASTER
        if (this == NONE) {
            return NONE;
        }

        ForgingQuality[] values = values();
        int index = this.ordinal();
        return index > 0 ? values[index - 1] : this; // POOR stays POOR
    }

    public static void downgradeDamageableItems(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        if (!stack.isDamageableItem()) return;
        CompoundTag tag = stack.getTag();

        String current = tag != null ? tag.getString("ForgingQuality") : "";
        ForgingQuality quality;

        if (current.isEmpty()) {
            // Check if item is affected by datapack
            boolean affected = QualityAttributeReloadListener.INSTANCE
                    .getAllItems()
                    .contains(stack.getItem());

            if (!affected) return;

            // Default to WELL
            quality = ForgingQuality.WELL;
        } else {
            quality = fromString(current);
        }

        ForgingQuality lower = quality.getLowerQuality();

        if (tag == null) {
            tag = new CompoundTag();
            stack.setTag(tag);
        }

        tag.putString("ForgingQuality", lower.getDisplayName());
    }
}

