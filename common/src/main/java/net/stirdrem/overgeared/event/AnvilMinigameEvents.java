package net.stirdrem.overgeared.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.stirdrem.overgeared.networking.PacketDistributor;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.networking.packet.SetMinigameVisibleC2SPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Client-side state and logic of the anvil forging minigame.
 * The arrow and zones live on a 0-100 scale; the overlay maps that onto the bar's pixels.
 */
public class AnvilMinigameEvents {
    // Bar scale
    private static final float BAR_MIN = 1f;
    private static final float BAR_MAX = 100f;

    // Zone re-placement after each hit
    private static final int MAX_PLACEMENT_ATTEMPTS = 30;
    private static final float MIN_CENTER_SHIFT = 5f; // new zone center must move at least this much
    private static final float MIN_EDGE_SHIFT = 3f;   // and each edge at least this much

    // Popups
    public static final float POPUP_DURATION_MS = 1500f;
    public static final float MS_PER_TICK = 50f;
    private static final Component POPUP_PERFECT = Component.translatable("overgeared.forging.perfect")
            .withStyle(s -> s.withBold(true).withColor(0xFFD700));
    private static final Component POPUP_GOOD = Component.translatable("overgeared.forging.good")
            .withStyle(s -> s.withBold(true).withColor(0x55FF55));
    private static final Component POPUP_MISS = Component.translatable("overgeared.forging.miss")
            .withStyle(s -> s.withBold(true).withColor(0xFF5555));
    private static final List<Popup> POPUPS = new ArrayList<>();

    // Session state
    private static boolean isVisible = false;
    public static boolean minigameStarted = false;
    public static boolean crouchReleasedSinceStart = false;
    public static int hitsRemaining = 0;
    public static int maxHits = 0;
    public static int perfectHits = 0;
    public static int goodHits = 0;
    public static int missedHits = 0;

    // Arrow
    public static float arrowPosition = 50;
    private static float prevArrowPosition = 50; // position at the previous tick, for render interpolation
    private static boolean movingDown = false;
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
    private static float currentPerfectZoneSize = 0;
    private static float currentGoodZoneSize = 0;

    /** Config values for one blueprint quality tier. */
    private record QualitySettings(ModConfigSpec.DoubleValue arrowSpeed,
                                   ModConfigSpec.DoubleValue arrowSpeedIncrease,
                                   ModConfigSpec.DoubleValue maxArrowSpeed,
                                   ModConfigSpec.DoubleValue zoneShrinkFactor,
                                   ModConfigSpec.IntValue zoneStartingSize,
                                   ModConfigSpec.IntValue minPerfectZone) {
    }

    private static QualitySettings settingsFor(String quality) {
        return switch (quality.toLowerCase(Locale.ROOT)) {
            case "none" -> new QualitySettings(ServerConfig.DEFAULT_ARROW_SPEED, ServerConfig.DEFAULT_ARROW_SPEED_INCREASE,
                    ServerConfig.DEFAULT_MAX_ARROW_SPEED, ServerConfig.DEFAULT_ZONE_SHRINK_FACTOR,
                    ServerConfig.DEFAULT_ZONE_STARTING_SIZE, ServerConfig.DEFAULT_MIN_PERFECT_ZONE);
            case "master" -> new QualitySettings(ServerConfig.MASTER_ARROW_SPEED, ServerConfig.MASTER_ARROW_SPEED_INCREASE,
                    ServerConfig.MASTER_MAX_ARROW_SPEED, ServerConfig.MASTER_ZONE_SHRINK_FACTOR,
                    ServerConfig.MASTER_ZONE_STARTING_SIZE, ServerConfig.MASTER_MIN_PERFECT_ZONE);
            case "perfect" -> new QualitySettings(ServerConfig.PERFECT_ARROW_SPEED, ServerConfig.PERFECT_ARROW_SPEED_INCREASE,
                    ServerConfig.PERFECT_MAX_ARROW_SPEED, ServerConfig.PERFECT_ZONE_SHRINK_FACTOR,
                    ServerConfig.PERFECT_ZONE_STARTING_SIZE, ServerConfig.PERFECT_MIN_PERFECT_ZONE);
            case "expert" -> new QualitySettings(ServerConfig.EXPERT_ARROW_SPEED, ServerConfig.EXPERT_ARROW_SPEED_INCREASE,
                    ServerConfig.EXPERT_MAX_ARROW_SPEED, ServerConfig.EXPERT_ZONE_SHRINK_FACTOR,
                    ServerConfig.EXPERT_ZONE_STARTING_SIZE, ServerConfig.EXPERT_MIN_PERFECT_ZONE);
            case "well" -> new QualitySettings(ServerConfig.WELL_ARROW_SPEED, ServerConfig.WELL_ARROW_SPEED_INCREASE,
                    ServerConfig.WELL_MAX_ARROW_SPEED, ServerConfig.WELL_ZONE_SHRINK_FACTOR,
                    ServerConfig.WELL_ZONE_STARTING_SIZE, ServerConfig.WELL_MIN_PERFECT_ZONE);
            default -> new QualitySettings(ServerConfig.POOR_ARROW_SPEED, ServerConfig.POOR_ARROW_SPEED_INCREASE,
                    ServerConfig.POOR_MAX_ARROW_SPEED, ServerConfig.POOR_ZONE_SHRINK_FACTOR,
                    ServerConfig.POOR_ZONE_STARTING_SIZE, ServerConfig.POOR_MIN_PERFECT_ZONE);
        };
    }

    public static void setupForQuality(String quality) {
        QualitySettings s = settingsFor(quality);
        int size = s.zoneStartingSize().get();

        arrowSpeed = s.arrowSpeed().get().floatValue();
        speedIncreasePerHit = s.arrowSpeedIncrease().get().floatValue();
        maxArrowSpeed = s.maxArrowSpeed().get().floatValue();
        zoneShrinkFactor = s.zoneShrinkFactor().get().floatValue();
        perfectZoneStart = (100 - size) / 2;
        perfectZoneEnd = (100 + size) / 2;
        goodZoneStart = Math.max((100 - size * 3) / 2, 1);
        goodZoneEnd = Math.min((100 + size * 3) / 2, 100);
        minPerfectSize = s.minPerfectZone().get();
    }

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Track crouch release so shift+click toggle requires a fresh shift press
        if (minigameStarted && !crouchReleasedSinceStart && !mc.player.isCrouching()) {
            crouchReleasedSinceStart = true;
        }
        if (mc.isPaused()) return;

        updatePopups();

        prevArrowPosition = arrowPosition;
        if (!isVisible) return;

        // Bounce between the bar ends
        if (arrowPosition >= BAR_MAX) {
            movingDown = true;
        } else if (arrowPosition <= BAR_MIN) {
            movingDown = false;
        }
        arrowPosition = Mth.clamp(arrowPosition + (movingDown ? -arrowSpeed : arrowSpeed), BAR_MIN, BAR_MAX);
    }

    private static void updatePopups() {
        for (Popup popup : POPUPS) {
            popup.age += MS_PER_TICK;
        }
        POPUPS.removeIf(popup -> popup.age >= POPUP_DURATION_MS);
    }

    public static void triggerPopup(Component text) {
        POPUPS.add(new Popup(text));
    }

    public static List<Popup> getPopups() {
        return POPUPS;
    }

    public static void resetPopUps() {
        POPUPS.clear();
    }

    public static void speedUp() {
        arrowSpeed = Math.min(arrowSpeed + speedIncreasePerHit, maxArrowSpeed);
    }

    public static float getArrowPosition() {
        return arrowPosition;
    }

    /** Arrow position interpolated between the last two ticks, for smooth rendering at any frame rate. */
    public static float getArrowPosition(float partialTick) {
        return Mth.lerp(partialTick, prevArrowPosition, arrowPosition);
    }

    public static boolean isVisible() {
        return isVisible;
    }

    public static void setIsVisible(BlockPos pos, boolean visible) {
        isVisible = visible;
        PacketDistributor.sendToServer(new SetMinigameVisibleC2SPacket(visible, pos));
    }

    public static void reset(String blueprintQuality) {
        isVisible = false;
        minigameStarted = false;
        crouchReleasedSinceStart = false;
        hitsRemaining = 0;
        perfectHits = 0;
        goodHits = 0;
        missedHits = 0;
        arrowPosition = 50;
        prevArrowPosition = 50;
        movingDown = false;
        currentPerfectZoneSize = 0;
        currentGoodZoneSize = 0;

        setupForQuality(blueprintQuality == null ? "none" : blueprintQuality);
        randomizeCenter();
    }

    public static void reset() {
        reset(null);
    }

    private static void randomizeCenter() {
        int center = (int) (20 + ThreadLocalRandom.current().nextFloat() * 60); // 20..80
        int halfPerfect = (perfectZoneEnd - perfectZoneStart) / 2;
        int halfGood = (goodZoneEnd - goodZoneStart) / 2;

        perfectZoneStart = Mth.clamp(center - halfPerfect, 0, 100);
        perfectZoneEnd = Mth.clamp(center + halfPerfect, 0, 100);
        goodZoneStart = Mth.clamp(center - halfGood, 0, 100);
        goodZoneEnd = Mth.clamp(center + halfGood, 0, 100);
    }

    public static int getPerfectZoneStart() {
        return perfectZoneStart;
    }

    public static int getPerfectZoneEnd() {
        return perfectZoneEnd;
    }

    public static int getGoodZoneStart() {
        return goodZoneStart;
    }

    public static int getGoodZoneEnd() {
        return goodZoneEnd;
    }

    public static int getHitsRemaining() {
        return hitsRemaining;
    }

    public static int getMaxHits() {
        return maxHits;
    }

    public static int getPerfectHits() {
        return perfectHits;
    }

    public static int getGoodHits() {
        return goodHits;
    }

    public static int getMissedHits() {
        return missedHits;
    }

    public static void setHitsRemaining(int hits) {
        hitsRemaining = hits;
        maxHits = hits;
    }

    public static String handleHit() {
        speedUp();

        if (arrowPosition >= perfectZoneStart && arrowPosition <= perfectZoneEnd) {
            perfectHits++;
            triggerPopup(POPUP_PERFECT);
        } else if (arrowPosition >= goodZoneStart && arrowPosition <= goodZoneEnd) {
            goodHits++;
            triggerPopup(POPUP_GOOD);
        } else {
            missedHits++;
            triggerPopup(POPUP_MISS);
        }

        shrinkAndShiftZones();
        hitsRemaining--;

        return hitsRemaining <= 0 ? finishForging() : "poor";
    }

    public static String finishForging() {
        isVisible = false;
        minigameStarted = false;
        int totalHits = perfectHits + goodHits + missedHits;
        float qualityScore = totalHits > 0 ? (perfectHits + goodHits * 0.6f) / totalHits : 0f;

        if (qualityScore > ServerConfig.PERFECT_QUALITY_SCORE.get()) return "perfect";
        if (qualityScore > ServerConfig.EXPERT_QUALITY_SCORE.get()) return "expert";
        if (qualityScore > ServerConfig.WELL_QUALITY_SCORE.get()) return "well";
        return "poor";
    }

    public static void shrinkAndShiftZones() {
        if (currentPerfectZoneSize == 0 || currentGoodZoneSize == 0) {
            currentPerfectZoneSize = perfectZoneEnd - perfectZoneStart;
            currentGoodZoneSize = goodZoneEnd - goodZoneStart;
        }

        // Shrink
        currentPerfectZoneSize = Math.max(minPerfectSize, currentPerfectZoneSize * zoneShrinkFactor);
        currentGoodZoneSize = Math.max(currentPerfectZoneSize * 3, currentGoodZoneSize * zoneShrinkFactor);

        float oldPerfectCenter = (perfectZoneStart + perfectZoneEnd) / 2f;
        float oldGoodCenter = (goodZoneStart + goodZoneEnd) / 2f;
        float halfPerfect = currentPerfectZoneSize / 2;
        float halfGood = currentGoodZoneSize / 2;

        // Move to a random spot clearly different from the old one; keep the old position if none is found
        for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS; attempt++) {
            float center = getWeightedRandomCenter(50f);

            int pStart = Mth.clamp(Math.round(center - halfPerfect), 0, 100);
            int pEnd = Mth.clamp(Math.round(center + halfPerfect), 0, 100);
            int gStart = Mth.clamp(Math.round(center - halfGood), 0, 100);
            int gEnd = Mth.clamp(Math.round(center + halfGood), 0, 100);

            if (tooClose(pStart, pEnd, perfectZoneStart, perfectZoneEnd, oldPerfectCenter)
                    || tooClose(gStart, gEnd, goodZoneStart, goodZoneEnd, oldGoodCenter)) {
                continue;
            }

            perfectZoneStart = pStart;
            perfectZoneEnd = pEnd;
            goodZoneStart = gStart;
            goodZoneEnd = gEnd;
            return;
        }
    }

    private static boolean tooClose(int newStart, int newEnd, int oldStart, int oldEnd, float oldCenter) {
        return Math.abs((newStart + newEnd) / 2f - oldCenter) < MIN_CENTER_SHIFT
                || Math.abs(newStart - oldStart) < MIN_EDGE_SHIFT
                || Math.abs(newEnd - oldEnd) < MIN_EDGE_SHIFT;
    }

    private static float getWeightedRandomCenter(float bias) {
        // Bias toward the middle but allow the full range; tweak the exponent for more/less bias
        float weighted = (float) Math.pow(ThreadLocalRandom.current().nextFloat(), 1.5);
        return bias + (weighted - 0.5f) * 100f;
    }

    public static BlockPos getAnvilPos(UUID playerId) {
        return ModItemInteractEvents.playerAnvilPositions.getOrDefault(playerId, BlockPos.ZERO);
    }

    public static void setAnvilPos(UUID playerId, BlockPos pos) {
        ModItemInteractEvents.playerAnvilPositions.put(playerId, pos);
    }

    public static boolean hasAnvilPosition(UUID playerId) {
        BlockPos pos = ModItemInteractEvents.playerAnvilPositions.get(playerId);
        return pos != null && !pos.equals(BlockPos.ZERO);
    }

    public static void setMinigameStarted(BlockPos pos, boolean started) {
        // Only reset crouch flag on fresh start (not on redundant S2C confirmations)
        if (started && !minigameStarted) {
            crouchReleasedSinceStart = false;
        }
        minigameStarted = started;
    }

    /** Player-specific hide. */
    public static void hideMinigame(UUID playerId) {
        isVisible = false;
        BlockPos pos = ModItemInteractEvents.playerAnvilPositions.get(playerId);
        if (pos != null && !pos.equals(BlockPos.ZERO)) {
            PacketDistributor.sendToServer(new SetMinigameVisibleC2SPacket(false, pos));
        }
    }

    public static class Popup {
        public final Component text;
        public float age; // milliseconds

        public Popup(Component text) {
            this.text = text;
        }
    }
}
