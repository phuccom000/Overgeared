package net.stirdrem.overgeared.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.stirdrem.overgeared.Overgeared;

import java.util.UUID;

public class AnvilMinigameEvents {
    public static void ensureInitialized() {
        if (ClientAnvilMinigameData.arrowSpeed == 0) {
            ClientAnvilMinigameData.setupForQuality("none");
        }
    }

    public static void onClientTick(Minecraft mc) {
        if (mc.player == null) return;
        if (!mc.isPaused()) updatePopups();
        if (mc.isPaused() || !isIsVisible()) return;

        ClientAnvilMinigameData.tickAccumulator++;
        if (ClientAnvilMinigameData.tickAccumulator < ClientAnvilMinigameData.TICKS_PER_PRINT) return;
        ClientAnvilMinigameData.tickAccumulator = 0;

        // Update arrow movement
        if (ClientAnvilMinigameData.arrowPosition >= 100) {
            ClientAnvilMinigameData.movingDown = true;
        } else if (ClientAnvilMinigameData.arrowPosition <= 1) {
            ClientAnvilMinigameData.movingDown = false;
        }

        // Determine movement based on current speed and direction
        float delta = ClientAnvilMinigameData.arrowSpeed * (ClientAnvilMinigameData.movingDown ? -1 : 1);
        ClientAnvilMinigameData.arrowPosition = Math.max(1, Math.min(ClientAnvilMinigameData.arrowPosition + delta, 100));
    }

    public static void updatePopups() {
        // Age existing popups
        for (int i = 0; i < ClientAnvilMinigameData.POPUPS.size(); i++) {
            ClientAnvilMinigameData.Popup popup = ClientAnvilMinigameData.POPUPS.get(i);
            popup.age += Minecraft.getInstance().getDeltaFrameTime() * 1000f;
            if (popup.age >= ClientAnvilMinigameData.POPUP_DURATION_MS) {
                ClientAnvilMinigameData.POPUPS.remove(i--);
            }
        }
    }

    public static void triggerPopup(Component text) {
        ClientAnvilMinigameData.POPUPS.add(new ClientAnvilMinigameData.Popup(text));
    }

    public static java.util.List<ClientAnvilMinigameData.Popup> getPopups() {
        return ClientAnvilMinigameData.POPUPS;
    }

    public static void speedUp() {
        ClientAnvilMinigameData.arrowSpeed = Math.min(ClientAnvilMinigameData.arrowSpeed + ClientAnvilMinigameData.speedIncreasePerHit, ClientAnvilMinigameData.maxArrowSpeed);
    }

    public static float getArrowPosition() {
        return ClientAnvilMinigameData.arrowPosition;
    }

    public static boolean isIsVisible() {
        return ClientAnvilMinigameData.isVisible;
    }

    public static void resetPopUps() {
        ClientAnvilMinigameData.POPUPS.clear();
    }

    public static void reset(String blueprintQuality) {
        ClientAnvilMinigameData.isVisible = false;
        ClientAnvilMinigameData.minigameStarted = false;
        ClientAnvilMinigameData.hitsRemaining = 0;
        ClientAnvilMinigameData.perfectHits = 0;
        ClientAnvilMinigameData.goodHits = 0;
        ClientAnvilMinigameData.missedHits = 0;
        ClientAnvilMinigameData.arrowPosition = 50;
        ClientAnvilMinigameData.movingDown = false;
        ClientAnvilMinigameData.currentPerfectZoneSize = 0;
        ClientAnvilMinigameData.currentGoodZoneSize = 0;
        ClientAnvilMinigameData.lastPerfect = 0;
        ClientAnvilMinigameData.lastGood = 0;
        ClientAnvilMinigameData.lastMiss = 0;

        ClientAnvilMinigameData.setupForQuality(blueprintQuality == null ? "none" : blueprintQuality);

        randomizeCenter();
    }

    public static void reset() {
        reset(null);
    }

    // Utility clamp
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void randomizeCenter() {
        // Randomize zone center directly
        float randomCenter = 20 + (float) Math.random() * (60); // random between 20 and 80
        float zoneSize = ClientAnvilMinigameData.perfectZoneEnd - ClientAnvilMinigameData.perfectZoneStart;
        float goodZoneSize = ClientAnvilMinigameData.goodZoneEnd - ClientAnvilMinigameData.goodZoneStart;

        int halfPerfect = (int) (zoneSize / 2f);
        int halfGood = (int) (goodZoneSize / 2f);

        ClientAnvilMinigameData.perfectZoneStart = clamp((int) randomCenter - halfPerfect, 0, 100);
        ClientAnvilMinigameData.perfectZoneEnd = clamp((int) randomCenter + halfPerfect, 0, 100);
        ClientAnvilMinigameData.goodZoneStart = clamp((int) randomCenter - halfGood, 0, 100);
        ClientAnvilMinigameData.goodZoneEnd = clamp((int) randomCenter + halfGood, 0, 100);
    }

    public static int getPerfectZoneStart() {
        return ClientAnvilMinigameData.perfectZoneStart;
    }

    public static int getPerfectZoneEnd() {
        return ClientAnvilMinigameData.perfectZoneEnd;
    }

    public static int getGoodZoneStart() {
        return ClientAnvilMinigameData.goodZoneStart;
    }

    public static int getGoodZoneEnd() {
        return ClientAnvilMinigameData.goodZoneEnd;
    }

    public static int getHitsRemaining() {
        return ClientAnvilMinigameData.hitsRemaining;
    }

    public static int getMaxHits() {
        return ClientAnvilMinigameData.maxHits;
    }

    public static int getPerfectHits() {
        return ClientAnvilMinigameData.perfectHits;
    }

    public static int getGoodHits() {
        return ClientAnvilMinigameData.goodHits;
    }

    public static int getMissedHits() {
        return ClientAnvilMinigameData.missedHits;
    }

    public static String handleHit() {
        ClientAnvilMinigameData.arrowSpeed = Math.min(ClientAnvilMinigameData.arrowSpeed + ClientAnvilMinigameData.speedIncreasePerHit, ClientAnvilMinigameData.maxArrowSpeed);

        if (ClientAnvilMinigameData.arrowPosition >= ClientAnvilMinigameData.perfectZoneStart && ClientAnvilMinigameData.arrowPosition <= ClientAnvilMinigameData.perfectZoneEnd) {
            ClientAnvilMinigameData.perfectHits++;
            ClientAnvilMinigameData.lastPerfect = ClientAnvilMinigameData.perfectHits;
            triggerPopup(Component.translatable("overgeared.forging.perfect")
                    .withStyle(s -> s.withBold(true).withColor(0xFFD700)));
        } else if (ClientAnvilMinigameData.arrowPosition >= ClientAnvilMinigameData.goodZoneStart && ClientAnvilMinigameData.arrowPosition <= ClientAnvilMinigameData.goodZoneEnd) {
            ClientAnvilMinigameData.goodHits++;
            ClientAnvilMinigameData.lastGood = ClientAnvilMinigameData.goodHits;
            triggerPopup(Component.translatable("overgeared.forging.good")
                    .withStyle(s -> s.withBold(true).withColor(0x55FF55)));
        } else {
            ClientAnvilMinigameData.missedHits++;
            ClientAnvilMinigameData.lastMiss = ClientAnvilMinigameData.missedHits;
            triggerPopup(Component.translatable("overgeared.forging.miss")
                    .withStyle(s -> s.withBold(true).withColor(0xFF5555)));
        }

        shrinkAndShiftZones();
        ClientAnvilMinigameData.hitsRemaining--;

        if (ClientAnvilMinigameData.hitsRemaining <= 0) {
            return finishForging();
        }
        return "poor";
    }

    public static void setHitsRemaining(int hitsRemaining) {
        ClientAnvilMinigameData.hitsRemaining = hitsRemaining;
        ClientAnvilMinigameData.maxHits = hitsRemaining;
    }

    public static String finishForging() {
        ClientAnvilMinigameData.isVisible = false;
        ClientAnvilMinigameData.minigameStarted = false;
        int totalHits = ClientAnvilMinigameData.perfectHits + ClientAnvilMinigameData.goodHits + ClientAnvilMinigameData.missedHits;
        float qualityScore = 0;
        if (totalHits > 0)
            qualityScore = (ClientAnvilMinigameData.perfectHits * 1.0f + ClientAnvilMinigameData.goodHits * 0.6f) / totalHits;
        if (qualityScore > Overgeared.getConfig().PERFECT_QUALITY_SCORE()) return "perfect";
        if (qualityScore > Overgeared.getConfig().EXPERT_QUALITY_SCORE()) return "expert";
        if (qualityScore > Overgeared.getConfig().WELL_QUALITY_SCORE()) return "well";
        return "poor";
    }

    public static void shrinkAndShiftZones() {
        // Initialize once if not yet done
        if (ClientAnvilMinigameData.currentPerfectZoneSize == 0 || ClientAnvilMinigameData.currentGoodZoneSize == 0) {
            ClientAnvilMinigameData.currentPerfectZoneSize = ClientAnvilMinigameData.perfectZoneEnd - ClientAnvilMinigameData.perfectZoneStart;
            ClientAnvilMinigameData.currentGoodZoneSize = ClientAnvilMinigameData.goodZoneEnd - ClientAnvilMinigameData.goodZoneStart;
        }

        // --- Step 1: Shrink zones based on stored sizes ---
        ClientAnvilMinigameData.currentPerfectZoneSize = Math.max(ClientAnvilMinigameData.minPerfectSize, ClientAnvilMinigameData.currentPerfectZoneSize * ClientAnvilMinigameData.zoneShrinkFactor);
        ClientAnvilMinigameData.currentGoodZoneSize = Math.max(ClientAnvilMinigameData.currentPerfectZoneSize * 3, ClientAnvilMinigameData.currentGoodZoneSize * ClientAnvilMinigameData.zoneShrinkFactor);

        // --- Step 2: Get old data for comparison ---
        float oldPerfectCenter = (ClientAnvilMinigameData.perfectZoneStart + ClientAnvilMinigameData.perfectZoneEnd) / 2f;
        float oldGoodCenter = (ClientAnvilMinigameData.goodZoneStart + ClientAnvilMinigameData.goodZoneEnd) / 2f;
        int oldPerfectStart = ClientAnvilMinigameData.perfectZoneStart;
        int oldPerfectEnd = ClientAnvilMinigameData.perfectZoneEnd;
        int oldGoodStart = ClientAnvilMinigameData.goodZoneStart;
        int oldGoodEnd = ClientAnvilMinigameData.goodZoneEnd;

        // --- Step 3: Attempt to find a new valid zone placement ---
        int attempts = 0;
        int newPerfectStart = ClientAnvilMinigameData.perfectZoneStart, newPerfectEnd = ClientAnvilMinigameData.perfectZoneEnd;
        int newGoodStart = ClientAnvilMinigameData.goodZoneStart, newGoodEnd = ClientAnvilMinigameData.goodZoneEnd;

        while (attempts < 30) {
            attempts++;

            // Weighted random center: bias near middle but allow full range
            float newCenter = getWeightedRandomCenter(50f);

            // Compute bounds
            int pStart = Math.round(newCenter - ClientAnvilMinigameData.currentPerfectZoneSize / 2);
            int pEnd = Math.round(newCenter + ClientAnvilMinigameData.currentPerfectZoneSize / 2);
            int gStart = Math.round(newCenter - ClientAnvilMinigameData.currentGoodZoneSize / 2);
            int gEnd = Math.round(newCenter + ClientAnvilMinigameData.currentGoodZoneSize / 2);

            // Clamp to 0–100 range
            pStart = clamp(pStart, 0, 100);
            pEnd = clamp(pEnd, 0, 100);
            gStart = clamp(gStart, 0, 100);
            gEnd = clamp(gEnd, 0, 100);

            float newPerfectCenter = (pStart + pEnd) / 2f;
            float newGoodCenter = (gStart + gEnd) / 2f;

            // --- Step 4: Check separation conditions ---
            float minCenterDiff = 5f;
            float minEdgeDiff = 3f;

            boolean perfectTooClose =
                    Math.abs(newPerfectCenter - oldPerfectCenter) < minCenterDiff ||
                            Math.abs(pStart - oldPerfectStart) < minEdgeDiff ||
                            Math.abs(pEnd - oldPerfectEnd) < minEdgeDiff;

            boolean goodTooClose =
                    Math.abs(newGoodCenter - oldGoodCenter) < minCenterDiff ||
                            Math.abs(gStart - oldGoodStart) < minEdgeDiff ||
                            Math.abs(gEnd - oldGoodEnd) < minEdgeDiff;

            if (perfectTooClose || goodTooClose) continue;

            newPerfectStart = pStart;
            newPerfectEnd = pEnd;
            newGoodStart = gStart;
            newGoodEnd = gEnd;
            break;
        }

        ClientAnvilMinigameData.perfectZoneStart = newPerfectStart;
        ClientAnvilMinigameData.perfectZoneEnd = newPerfectEnd;
        ClientAnvilMinigameData.goodZoneStart = newGoodStart;
        ClientAnvilMinigameData.goodZoneEnd = newGoodEnd;
    }

    public static float getWeightedRandomCenter(float bias) {
        float rand = (float) Math.random();
        float weighted = (float) Math.pow(rand, 1.5);
        return bias + (weighted - 0.5f) * 100f;
    }

    public static BlockPos getAnvilPos(UUID playerId) {
        return ClientAnvilMinigameData.playerAnvilPositions.getOrDefault(playerId, BlockPos.ZERO);
    }

    public static void setAnvilPos(UUID playerId, BlockPos pos) {
        ClientAnvilMinigameData.playerAnvilPositions.put(playerId, pos);
    }

    public static void clearAnvilPos(UUID playerId) {
        ClientAnvilMinigameData.playerAnvilPositions.remove(playerId);
    }

    public static void setMinigameStarted(BlockPos pos, boolean minigameStarted) {
        ClientAnvilMinigameData.minigameStarted = minigameStarted;
    }

    public static UUID getOccupiedAnvil(BlockPos pos) {
        return ClientAnvilMinigameData.occupiedAnvils.get(pos);
    }

    public static void putOccupiedAnvil(BlockPos pos, UUID me) {
        ClientAnvilMinigameData.occupiedAnvils.put(pos, me);
    }

    public static boolean hasAnvilPosition(UUID playerId) {
        BlockPos pos = ClientAnvilMinigameData.playerAnvilPositions.get(playerId);
        return pos != null && !pos.equals(BlockPos.ZERO);
    }
}
