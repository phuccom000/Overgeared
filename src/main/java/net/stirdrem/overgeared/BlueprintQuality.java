package net.stirdrem.overgeared;

import net.minecraft.util.Formatting;
import net.stirdrem.overgeared.config.ServerConfig;

import java.util.Locale;

public enum BlueprintQuality {
    POOR("poor", ServerConfig.POOR_MAX_USE.get(), Formatting.RED),
    WELL("well", ServerConfig.WELL_MAX_USE.get(), Formatting.YELLOW),
    EXPERT("expert", ServerConfig.EXPERT_MAX_USE.get(), Formatting.BLUE),
    PERFECT("perfect", 0, Formatting.GOLD),
    MASTER("master", 0, Formatting.LIGHT_PURPLE); // Final tier

    private final String id;
    private final int use;
    private final Formatting color;

    BlueprintQuality(String id, int use, Formatting color) {
        this.id = id;
        this.use = use;
        this.color = color;
    }

    public static int compare(String q1, String q2) {
        BlueprintQuality a = fromString(q1);
        BlueprintQuality b = fromString(q2);
        return Integer.compare(a.ordinal(), b.ordinal());
    }

    /**
     * Match a quality string safely.
     */
    public static BlueprintQuality fromString(String id) {
        for (BlueprintQuality q : values()) {
            if (q.id.equalsIgnoreCase(id)) return q;
        }
        return POOR; // fallback
    }

    /**
     * Get the next tier of blueprint quality.
     */
    public static BlueprintQuality getNext(BlueprintQuality current) {
        int index = current.ordinal();
        if (index + 1 < values().length) {
            return values()[index + 1];
        }
        return null; // Already at max
    }

    /**
     * Get the previous tier of blueprint quality.
     */
    public static BlueprintQuality getPrevious(BlueprintQuality current) {
        int index = current.ordinal();
        if (index - 1 >= 0) {
            return values()[index - 1];
        }
        return null; // Already at lowest
    }

    public static Formatting getColor(String qualityName) {
        for (BlueprintQuality q : values()) {
            if (q.name().equalsIgnoreCase(qualityName)) {
                return q.color;
            }
        }
        return Formatting.GRAY;
    }

    public String getDisplayName() {
        return id;
    }

    public int getUse() {
        return use;
    }

    public Formatting getColor() {
        return color;
    }

    public String getTranslationKey() {
        return "tooltip.overgeared.blueprint.quality." + name().toLowerCase(Locale.ROOT);
    }

    public String getId() {
        return id;
    }

}
