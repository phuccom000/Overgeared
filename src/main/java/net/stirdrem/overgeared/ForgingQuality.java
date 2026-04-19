package net.stirdrem.overgeared;

import net.minecraft.world.item.ItemStack;

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

    public static void downgrade(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return;

        String current = stack.getTag().getString("ForgingQuality");
        if (current.isEmpty()) return;

        ForgingQuality quality = fromString(current);
        ForgingQuality lower = quality.getLowerQuality();

        // If already lowest (POOR or NONE), remove or clamp
        if (quality == lower) {
            // Your design choice:
            // OPTION 1: keep POOR
            if (quality == POOR) return;

            // OPTION 2: remove quality entirely
            stack.getTag().remove("ForgingQuality");
            return;
        }

        // Apply downgraded quality
        stack.getTag().putString("ForgingQuality", lower.getDisplayName());
    }
}

