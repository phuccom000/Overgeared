package net.stirdrem.overgeared.client;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.config.ServerConfigCommon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side state of the anvil forging minigame. The logic lives in {@link AnvilMinigameEvents}; the arrow and
 * zones use a 0-100 scale that the HUD maps onto the bar.
 */
public class ClientAnvilMinigameData {
    public static final Map<UUID, BlockPos> playerAnvilPositions = new HashMap<>();
    public static final Map<UUID, Boolean> playerMinigameVisibility = new HashMap<>();
    public static final Map<BlockPos, UUID> occupiedAnvils = Collections.synchronizedMap(new HashMap<>());

    // Popups (aged once per client tick, see AnvilMinigameEvents)
    public static final List<Popup> POPUPS = new ArrayList<>();
    public static final float POPUP_DURATION_MS = 1500f;
    public static final float MS_PER_TICK = 50f;

    // Session
    public static boolean isVisible = false;
    public static boolean minigameStarted = false;
    public static int hitsRemaining = 0;
    public static int maxHits = 0;
    public static int perfectHits = 0;
    public static int goodHits = 0;
    public static int missedHits = 0;

    // Arrow
    public static float arrowPosition = 1;
    /** Arrow position at the previous tick, so the HUD can interpolate between ticks. */
    public static float prevArrowPosition = 1;
    public static boolean movingDown = false;
    public static float arrowSpeed = 0;
    public static float maxArrowSpeed = 0;
    public static float speedIncreasePerHit = 0;

    // Zones
    public static int perfectZoneStart = 45;
    public static int perfectZoneEnd = 55;
    public static int goodZoneStart = 35;
    public static int goodZoneEnd = 65;
    public static float zoneShrinkFactor = 0.95f;
    public static float minPerfectSize = 4;
    // Unclamped sizes, so zones pinned against a bar edge still shrink correctly
    public static float currentPerfectZoneSize = 0;
    public static float currentGoodZoneSize = 0;

    private static BlockPos pendingMinigamePos = null;

    public static UUID getOccupiedAnvil(BlockPos pos) {
        return occupiedAnvils.get(pos);
    }

    public static void putOccupiedAnvil(BlockPos pos, UUID owner) {
        occupiedAnvils.put(pos, owner);
    }

    public static void setPendingMinigame(BlockPos pos) {
        pendingMinigamePos = pos;
    }

    public static BlockPos getPendingMinigamePos() {
        return pendingMinigamePos;
    }

    public static void clearPendingMinigame() {
        pendingMinigamePos = null;
    }

    /** Minigame tuning for one blueprint quality tier, read from the server config. */
    private record QualitySettings(double arrowSpeed, double arrowSpeedIncrease, double maxArrowSpeed,
                                   double zoneShrinkFactor, int zoneStartingSize, int minPerfectZone) {
    }

    private static QualitySettings settingsFor(String quality) {
        ServerConfigCommon c = Overgeared.getConfig();
        return switch (quality.toLowerCase(Locale.ROOT)) {
            case "none" -> new QualitySettings(c.DEFAULT_ARROW_SPEED(), c.DEFAULT_ARROW_SPEED_INCREASE(),
                    c.DEFAULT_MAX_ARROW_SPEED(), c.DEFAULT_ZONE_SHRINK_FACTOR(), c.DEFAULT_ZONE_STARTING_SIZE(), c.DEFAULT_MIN_PERFECT_ZONE());
            case "master" -> new QualitySettings(c.MASTER_ARROW_SPEED(), c.MASTER_ARROW_SPEED_INCREASE(),
                    c.MASTER_MAX_ARROW_SPEED(), c.MASTER_ZONE_SHRINK_FACTOR(), c.MASTER_ZONE_STARTING_SIZE(), c.MASTER_MIN_PERFECT_ZONE());
            case "perfect" -> new QualitySettings(c.PERFECT_ARROW_SPEED(), c.PERFECT_ARROW_SPEED_INCREASE(),
                    c.PERFECT_MAX_ARROW_SPEED(), c.PERFECT_ZONE_SHRINK_FACTOR(), c.PERFECT_ZONE_STARTING_SIZE(), c.PERFECT_MIN_PERFECT_ZONE());
            case "expert" -> new QualitySettings(c.EXPERT_ARROW_SPEED(), c.EXPERT_ARROW_SPEED_INCREASE(),
                    c.EXPERT_MAX_ARROW_SPEED(), c.EXPERT_ZONE_SHRINK_FACTOR(), c.EXPERT_ZONE_STARTING_SIZE(), c.EXPERT_MIN_PERFECT_ZONE());
            case "well" -> new QualitySettings(c.WELL_ARROW_SPEED(), c.WELL_ARROW_SPEED_INCREASE(),
                    c.WELL_MAX_ARROW_SPEED(), c.WELL_ZONE_SHRINK_FACTOR(), c.WELL_ZONE_STARTING_SIZE(), c.WELL_MIN_PERFECT_ZONE());
            default -> new QualitySettings(c.POOR_ARROW_SPEED(), c.POOR_ARROW_SPEED_INCREASE(),
                    c.POOR_MAX_ARROW_SPEED(), c.POOR_ZONE_SHRINK_FACTOR(), c.POOR_ZONE_STARTING_SIZE(), c.POOR_MIN_PERFECT_ZONE());
        };
    }

    public static void setupForQuality(String quality) {
        QualitySettings s = settingsFor(quality);
        int size = s.zoneStartingSize();

        arrowSpeed = (float) s.arrowSpeed();
        speedIncreasePerHit = (float) s.arrowSpeedIncrease();
        maxArrowSpeed = (float) s.maxArrowSpeed();
        zoneShrinkFactor = (float) s.zoneShrinkFactor();
        perfectZoneStart = (100 - size) / 2;
        perfectZoneEnd = (100 + size) / 2;
        goodZoneStart = Math.max((100 - size * 3) / 2, 1);
        goodZoneEnd = Math.min((100 + size * 3) / 2, 100);
        minPerfectSize = s.minPerfectZone();
    }

    public static class Popup {
        public final Component text;
        public float age; // milliseconds

        public Popup(Component text) {
            this.text = text;
        }
    }
}
