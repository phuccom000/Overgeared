package net.stirdrem.overgeared;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;

import java.util.Locale;
import java.util.function.Supplier;

public enum ForgingQuality implements StringRepresentable {
    POOR("poor", ChatFormatting.RED, 10, () -> ServerConfig.POOR_MAX_USE),
    WELL("well", ChatFormatting.YELLOW, 15, () -> ServerConfig.WELL_MAX_USE),
    EXPERT("expert", ChatFormatting.BLUE, 20, () -> ServerConfig.EXPERT_MAX_USE),
    PERFECT("perfect", ChatFormatting.GOLD, 40, () -> ServerConfig.PERFECT_MAX_USE),
    MASTER("master", ChatFormatting.LIGHT_PURPLE, 30, null), // Final tier
    NONE("none", ChatFormatting.GRAY, 0, null);

    private final String displayName;
    private final ChatFormatting color;
    /** Fallback blueprint uses-to-next-level, used before the config is loaded. */
    private final int defaultBlueprintUses;
    private final Supplier<ModConfigSpec.IntValue> blueprintUsesConfig;

    // Codec for persistence (saves as string)
    public static final Codec<ForgingQuality> CODEC = Codec.STRING.xmap(
            ForgingQuality::fromString,
            ForgingQuality::getDisplayName
    );

    // StreamCodec for network synchronization
    public static final StreamCodec<ByteBuf, ForgingQuality> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(
            ForgingQuality::fromString,
            ForgingQuality::getDisplayName
    );

    ForgingQuality(String displayName, ChatFormatting color, int defaultBlueprintUses,
                   Supplier<ModConfigSpec.IntValue> blueprintUsesConfig) {
        this.displayName = displayName;
        this.color = color;
        this.defaultBlueprintUses = defaultBlueprintUses;
        this.blueprintUsesConfig = blueprintUsesConfig;
    }

    public static ForgingQuality fromString(String quality) {
        return fromString(quality, POOR);
    }

    /**
     * Match a quality string case-insensitively, returning {@code fallback} when nothing matches.
     */
    public static ForgingQuality fromString(String quality, ForgingQuality fallback) {
        for (ForgingQuality q : values()) {
            if (q.displayName.equalsIgnoreCase(quality)) return q;
        }
        return fallback;
    }

    /**
     * Match a blueprint quality string; unknown or empty strings mean no quality.
     */
    public static ForgingQuality fromBlueprintString(String quality) {
        return fromString(quality, NONE);
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSerializedName() {
        return displayName;
    }

    /**
     * Returns the durability multiplier for this quality level based on config values.
     */
    public float getDurabilityMultiplier() {
        return switch (this) {
            case POOR -> ServerConfig.POOR_DURABILITY_BONUS.get().floatValue();
            case WELL -> ServerConfig.WELL_DURABILITY_BONUS.get().floatValue();
            case EXPERT -> ServerConfig.EXPERT_DURABILITY_BONUS.get().floatValue();
            case PERFECT -> ServerConfig.PERFECT_DURABILITY_BONUS.get().floatValue();
            case MASTER -> ServerConfig.MASTER_DURABILITY_BONUS.get().floatValue();
            case NONE -> 1.0f;
        };
    }

    public float getMiningSpeedMultiplier() {
        return switch (this) {
            case POOR -> ServerConfig.POOR_MINING_SPEED_BONUS.get().floatValue();
            case WELL -> ServerConfig.WELL_MINING_SPEED_BONUS.get().floatValue();
            case EXPERT -> ServerConfig.EXPERT_MINING_SPEED_BONUS.get().floatValue();
            case PERFECT -> ServerConfig.PERFECT_MINING_SPEED_BONUS.get().floatValue();
            case MASTER -> ServerConfig.MASTER_MINING_SPEED_BONUS.get().floatValue();
            case NONE -> 1.0f;
        };
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

    public ChatFormatting getColor() {
        return color;
    }

    public static ChatFormatting getColor(String qualityName) {
        return fromBlueprintString(qualityName).getColor();
    }

    /** Translation key for the blueprint tooltip's quality name. */
    public String getBlueprintTranslationKey() {
        return "tooltip.overgeared.blueprint.quality." + name().toLowerCase(Locale.ROOT);
    }

    // --- Blueprint progression ---

    /**
     * The next blueprint tier. NONE goes to POOR; MASTER stays MASTER.
     */
    public static ForgingQuality getNext(ForgingQuality current) {
        return switch (current) {
            case NONE -> POOR;
            case POOR -> WELL;
            case WELL -> EXPERT;
            case EXPERT -> PERFECT;
            case PERFECT, MASTER -> MASTER;
        };
    }

    /**
     * The previous blueprint tier. POOR (and NONE) go to NONE.
     */
    public static ForgingQuality getPrevious(ForgingQuality current) {
        return switch (current) {
            case NONE, POOR -> NONE;
            case WELL -> POOR;
            case EXPERT -> WELL;
            case PERFECT -> EXPERT;
            case MASTER -> PERFECT;
        };
    }

    /**
     * Uses a blueprint of this quality needs to reach the next quality.
     */
    public int getBlueprintUses() {
        if (this == NONE)
            return 0;
        // MASTER is the final tier and never levels up, so it has no config value
        if (blueprintUsesConfig == null)
            return defaultBlueprintUses;

        try {
            return blueprintUsesConfig.get().get();
        } catch (IllegalStateException e) {
            // Config not loaded yet, return default
            return defaultBlueprintUses;
        }
    }

    /**
     * Whether this is the highest quality a blueprint can level up to.
     * PERFECT only counts as max when Perfect -> Master upgrading is disabled in the config.
     */
    public boolean isMaxBlueprintLevel() {
        if (this == MASTER) return true;
        if (this != PERFECT) return false;
        try {
            return !ServerConfig.PERFECT_BLUEPRINT_UPGRADE_TO_MASTER.get();
        } catch (IllegalStateException e) {
            return true; // Config not loaded yet
        }
    }

    public static void downgrade(ItemStack stack) {
        if (stack == null) return;

        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY.get());
        if (quality == null) {
            // Check if item is affected by datapack
            boolean affected = QualityAttributeReloadListener.INSTANCE
                    .getAllItems()
                    .contains(stack.getItem());

            if (!affected) return;

            // Default to WELL
            quality = ForgingQuality.WELL;
        }
        ForgingQuality lower = quality.getLowerQuality();
        // Apply downgraded quality
        stack.set(ModComponents.FORGING_QUALITY.get(), lower);
    }
}

