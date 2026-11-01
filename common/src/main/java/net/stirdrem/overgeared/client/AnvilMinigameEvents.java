package net.stirdrem.overgeared.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.stirdrem.overgeared.Overgeared;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static net.stirdrem.overgeared.client.ClientAnvilMinigameData.*;

/**
 * Client-side logic of the anvil forging minigame (state in {@link ClientAnvilMinigameData}).
 * Ticked by each loader's client tick hook; drawn by {@link AnvilMinigameHud}.
 */
public class AnvilMinigameEvents {
    private static final float BAR_MIN = 1f;
    private static final float BAR_MAX = 100f;

    // Zone re-placement after each hit
    private static final int MAX_PLACEMENT_ATTEMPTS = 30;
    private static final float MIN_CENTER_SHIFT = 5f; // new zone center must move at least this much
    private static final float MIN_EDGE_SHIFT = 3f;   // and each edge at least this much

    private static final Component POPUP_PERFECT = Component.translatable("overgeared.forging.perfect")
            .withStyle(s -> s.withBold(true).withColor(0xFFD700));
    private static final Component POPUP_GOOD = Component.translatable("overgeared.forging.good")
            .withStyle(s -> s.withBold(true).withColor(0x55FF55));
    private static final Component POPUP_MISS = Component.translatable("overgeared.forging.miss")
            .withStyle(s -> s.withBold(true).withColor(0xFF5555));

    public static void ensureInitialized() {
        if (arrowSpeed == 0) {
            setupForQuality("none");
        }
    }

    public static void onClientTick(Minecraft mc) {
        if (mc.player == null || mc.isPaused()) return;

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

    /** Ages popups by a fixed amount per tick (the HUD adds the partial tick), so their lifetime doesn't depend on FPS. */
    public static void updatePopups() {
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

    public static boolean isIsVisible() {
        return isVisible;
    }

    public static void reset(String blueprintQuality) {
        isVisible = false;
        minigameStarted = false;
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

    public static void randomizeCenter() {
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

    public static void setHitsRemaining(int hits) {
        hitsRemaining = hits;
        maxHits = hits;
    }

    public static String finishForging() {
        isVisible = false;
        minigameStarted = false;
        int totalHits = perfectHits + goodHits + missedHits;
        float qualityScore = totalHits > 0 ? (perfectHits + goodHits * 0.6f) / totalHits : 0f;

        if (qualityScore > Overgeared.getConfig().PERFECT_QUALITY_SCORE()) return "perfect";
        if (qualityScore > Overgeared.getConfig().EXPERT_QUALITY_SCORE()) return "expert";
        if (qualityScore > Overgeared.getConfig().WELL_QUALITY_SCORE()) return "well";
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

    public static float getWeightedRandomCenter(float bias) {
        // Bias toward the middle but allow the full range; tweak the exponent for more/less bias
        float weighted = (float) Math.pow(ThreadLocalRandom.current().nextFloat(), 1.5);
        return bias + (weighted - 0.5f) * 100f;
    }

    public static BlockPos getAnvilPos(UUID playerId) {
        return playerAnvilPositions.getOrDefault(playerId, BlockPos.ZERO);
    }

    public static void setAnvilPos(UUID playerId, BlockPos pos) {
        playerAnvilPositions.put(playerId, pos);
    }

    public static void clearAnvilPos(UUID playerId) {
        playerAnvilPositions.remove(playerId);
    }

    public static void setMinigameStarted(BlockPos pos, boolean started) {
        minigameStarted = started;
    }

    public static UUID getOccupiedAnvil(BlockPos pos) {
        return occupiedAnvils.get(pos);
    }

    public static void putOccupiedAnvil(BlockPos pos, UUID owner) {
        occupiedAnvils.put(pos, owner);
    }

    public static boolean hasAnvilPosition(UUID playerId) {
        BlockPos pos = playerAnvilPositions.get(playerId);
        return pos != null && !pos.equals(BlockPos.ZERO);
    }
}
