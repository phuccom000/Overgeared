package net.stirdrem.overgeared;

import net.minecraft.util.StringRepresentable;

public enum AnvilTier implements StringRepresentable {
    STONE("stone", "gui.overgeared.tier.stone"),
    IRON("iron", "gui.overgeared.tier.iron"),
    ABOVE_A("above_a", "gui.overgeared.tier.tier_a"),
    ABOVE_B("above_b", "gui.overgeared.tier.tier_b");

    private final String displayName;
    private final String lang;

    AnvilTier(String displayName, String lang) {
        this.displayName = displayName;
        this.lang = lang;
    }

    @Override
    public String getSerializedName() {
        return displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLang() {
        return lang;
    }

    public static AnvilTier fromDisplayName(String name) {
        for (AnvilTier tier : values()) {
            if (tier.displayName.equalsIgnoreCase(name)) {
                return tier;
            }
        }
        return null; // or throw IllegalArgumentException
    }

    public boolean isEqualOrLowerThan(AnvilTier other) {
        return this.ordinal() <= other.ordinal();
    }
}

