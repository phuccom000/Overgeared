package net.stirdrem.overgeared;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;

import java.util.Locale;

/**
 * Quality tier shared by forged items and blueprints.
 * <p>
 * NONE is a sentinel ("no quality") and is never a valid blueprint tier; the
 * navigation helpers ({@link #getNext}, {@link #getPrevious}, {@link #tiers()})
 * skip it.
 */
public enum ForgingQuality {
    POOR("poor", ChatFormatting.RED),
    WELL("well", ChatFormatting.YELLOW),
    EXPERT("expert", ChatFormatting.BLUE),
    PERFECT("perfect", ChatFormatting.GOLD),
    MASTER("master", ChatFormatting.LIGHT_PURPLE),
    NONE("none", ChatFormatting.GRAY);

    private static final ForgingQuality[] TIERS = {POOR, WELL, EXPERT, PERFECT, MASTER};

    private final String displayName;
    private final ChatFormatting color;

    ForgingQuality(String displayName, ChatFormatting color) {
        this.displayName = displayName;
        this.color = color;
    }

    /** Case-insensitive lookup by id ("poor") or enum name ("POOR"). Falls back to POOR. */
    public static ForgingQuality fromString(String quality) {
        for (ForgingQuality q : values()) {
            if (q.displayName.equalsIgnoreCase(quality)) return q;
        }
        return POOR; // fallback
    }

    /** All real tiers, lowest to highest (excludes NONE). */
    public static ForgingQuality[] tiers() {
        return TIERS.clone();
    }

    public static int compare(String q1, String q2) {
        return Integer.compare(fromString(q1).ordinal(), fromString(q2).ordinal());
    }

    /** Next tier up, or null if already at MASTER (or NONE). */
    public static ForgingQuality getNext(ForgingQuality current) {
        if (current == NONE || current == MASTER) return null;
        return values()[current.ordinal() + 1];
    }

    /** Next tier down, or null if already at POOR (or NONE). */
    public static ForgingQuality getPrevious(ForgingQuality current) {
        if (current == NONE || current == POOR) return null;
        return values()[current.ordinal() - 1];
    }

    public static ChatFormatting getColor(String qualityName) {
        for (ForgingQuality q : values()) {
            if (q.displayName.equalsIgnoreCase(qualityName)) return q.color;
        }
        return ChatFormatting.GRAY;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return displayName;
    }

    public ChatFormatting getColor() {
        return color;
    }

    /**
     * True for tiers at which a blueprint stops levelling up. Perfect is a max tier
     * unless {@code perfectBlueprintUpgradeToMaster} is enabled in the server config.
     */
    public boolean isMaxBlueprintTier() {
        if (this == PERFECT) return !Overgeared.getConfig().PERFECT_BLUEPRINT_UPGRADE_TO_MASTER();
        return this == MASTER || this == NONE;
    }

    /** Uses required for a blueprint of this quality to reach the next tier (0 = max). */
    public int getBlueprintUses() {
        return switch (this) {
            case POOR -> Overgeared.getConfig().POOR_MAX_USE();
            case WELL -> Overgeared.getConfig().WELL_MAX_USE();
            case EXPERT -> Overgeared.getConfig().EXPERT_MAX_USE();
            case PERFECT -> isMaxBlueprintTier() ? 0 : Overgeared.getConfig().PERFECT_MAX_USE();
            default -> 0;
        };
    }

    public String getBlueprintTranslationKey() {
        return "tooltip.overgeared.blueprint.quality." + displayName.toLowerCase(Locale.ROOT);
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
