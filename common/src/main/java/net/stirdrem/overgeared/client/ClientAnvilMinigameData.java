package net.stirdrem.overgeared.client;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.Overgeared;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientAnvilMinigameData {
    public static final Map<UUID, BlockPos> playerAnvilPositions = new HashMap<>();
    public static final Map<UUID, Boolean> playerMinigameVisibility = new HashMap<>();
    public static final java.util.List<Popup> POPUPS = new java.util.ArrayList<>();
    public static final float POPUP_DURATION_MS = 10000f;
    public static final int TICKS_PER_PRINT = 1;
    public static UUID ownerUUID = null;
    public static boolean minigameStarted = false;
    public static ItemStack resultItem = null;
    public static int hitsRemaining = 0;
    public static int maxHits = 0;
    public static float arrowPosition = 1;
    // Initialize with placeholder defaults (will be overridden later)
    public static float arrowSpeed = 0;
    public static float maxArrowSpeed = 0;
    public static float speedIncreasePerHit = 0;
    public static boolean movingRight = true;
    public static int perfectHits = 0;
    public static int goodHits = 0;
    public static int missedHits = 0;
    public static int perfectZoneStart = 45;
    public static int perfectZoneEnd = 55;
    public static float perfectZoneSize = perfectZoneEnd - perfectZoneStart;
    public static int goodZoneStart = 35;
    public static int goodZoneEnd = 65;
    public static float zoneShrinkFactor = 0.95f;
    public static float zoneShiftAmount = 15.0f;
    public static float minPerfectSize = 4;
    public static Map<BlockPos, UUID> occupiedAnvils = Collections.synchronizedMap(new HashMap<>());
    public static int skillLevel = 0;
    public static boolean isVisible = false;
    public static int tickAccumulator = 0;
    public static boolean movingDown = false;
    public static int lastPerfect = 0;
    public static int lastGood = 0;
    public static int lastMiss = 0;
    // Add two static fields to track current sizes independently of clamped values:
    public static float currentPerfectZoneSize = 0;
    public static float currentGoodZoneSize = 0;


    // Visibility
    public static void setIsVisible(boolean visible) {
        ClientAnvilMinigameData.isVisible = visible;
    }

    public static boolean getIsVisible() {
        return ClientAnvilMinigameData.isVisible;
    }

    // Result item
    public static void setResultItem(ItemStack item) {
        ClientAnvilMinigameData.resultItem = item;
    }

    public static ItemStack getResultItem() {
        return ClientAnvilMinigameData.resultItem;
    }

    // Hits remaining
    public static void setHitsRemaining(int hits) {
        ClientAnvilMinigameData.hitsRemaining = hits;
    }

    public static int getHitsRemaining() {
        return ClientAnvilMinigameData.hitsRemaining;
    }

    // Arrow position
    public static void setArrowPosition(float position) {
        ClientAnvilMinigameData.arrowPosition = position;
    }

    public static float getArrowPosition() {
        return ClientAnvilMinigameData.arrowPosition;
    }

    // Arrow speed
    public static void setArrowSpeed(float speed) {
        ClientAnvilMinigameData.arrowSpeed = speed;
    }

    public static float getArrowSpeed() {
        return ClientAnvilMinigameData.arrowSpeed;
    }

    // Max arrow speed
    public static float getMaxArrowSpeed() {
        return ClientAnvilMinigameData.maxArrowSpeed;
    }

    // Speed increase per hit
    public static void setSpeedIncreasePerHit(float increase) {
        ClientAnvilMinigameData.speedIncreasePerHit = increase;
    }

    public static float getSpeedIncreasePerHit() {
        return ClientAnvilMinigameData.speedIncreasePerHit;
    }

    // Arrow direction
    public static void setMovingRight(boolean right) {
        ClientAnvilMinigameData.movingRight = right;
    }

    public static boolean isMovingRight() {
        return ClientAnvilMinigameData.movingRight;
    }

    // Perfect hits
    public static void setPerfectHits(int hits) {
        ClientAnvilMinigameData.perfectHits = hits;
    }

    public static int getPerfectHits() {
        return ClientAnvilMinigameData.perfectHits;
    }

    // Good hits
    public static void setGoodHits(int hits) {
        ClientAnvilMinigameData.goodHits = hits;
    }

    public static int getGoodHits() {
        return ClientAnvilMinigameData.goodHits;
    }

    // Missed hits
    public static void setMissedHits(int hits) {
        ClientAnvilMinigameData.missedHits = hits;
    }

    public static int getMissedHits() {
        return ClientAnvilMinigameData.missedHits;
    }

    // Perfect zone
    public static void setPerfectZoneStart(int start) {
        ClientAnvilMinigameData.perfectZoneStart = start;
    }

    public static int getPerfectZoneStart() {
        return ClientAnvilMinigameData.perfectZoneStart;
    }

    public static void setPerfectZoneEnd(int end) {
        ClientAnvilMinigameData.perfectZoneEnd = end;
    }

    public static int getPerfectZoneEnd() {
        return ClientAnvilMinigameData.perfectZoneEnd;
    }

    // Good zone
    public static void setGoodZoneStart(int start) {
        ClientAnvilMinigameData.goodZoneStart = start;
    }

    public static int getGoodZoneStart() {
        return ClientAnvilMinigameData.goodZoneStart;
    }

    public static void setGoodZoneEnd(int end) {
        ClientAnvilMinigameData.goodZoneEnd = end;
    }

    public static int getGoodZoneEnd() {
        return ClientAnvilMinigameData.goodZoneEnd;
    }

    // Zone shrink factor
    public static void setZoneShrinkFactor(float factor) {
        ClientAnvilMinigameData.zoneShrinkFactor = factor;
    }

    public static float getZoneShrinkFactor() {
        return ClientAnvilMinigameData.zoneShrinkFactor;
    }

    // Zone shift amount
    public static void setZoneShiftAmount(float amount) {
        ClientAnvilMinigameData.zoneShiftAmount = amount;
    }

    public static float getZoneShiftAmount() {
        return ClientAnvilMinigameData.zoneShiftAmount;
    }

    public static void loadFromNBT(CompoundTag nbt) {
        isVisible = nbt.contains("isVisible") && nbt.getBoolean("isVisible");

        if (nbt.hasUUID("ownerUUID")) {
            ownerUUID = nbt.getUUID("ownerUUID");
        } else {
            ownerUUID = null;
        }

        minigameStarted = nbt.contains("minigameStarted") && nbt.getBoolean("minigameStarted");

        if (nbt.contains("resultItem")) {
            resultItem = ItemStack.of(nbt.getCompound("resultItem"));
        } else {
            resultItem = ItemStack.EMPTY;
        }

        hitsRemaining = nbt.contains("hitsRemaining") ? nbt.getInt("hitsRemaining") : 0;
        perfectHits = nbt.contains("perfectHits") ? nbt.getInt("perfectHits") : 0;
        goodHits = nbt.contains("goodHits") ? nbt.getInt("goodHits") : 0;
        missedHits = nbt.contains("missedHits") ? nbt.getInt("missedHits") : 0;

        arrowPosition = nbt.contains("arrowPosition") ? nbt.getFloat("arrowPosition") : 0f;
        arrowSpeed = nbt.contains("arrowSpeed") ? nbt.getFloat("arrowSpeed") : (float) Overgeared.getConfig().POOR_ARROW_SPEED();
        speedIncreasePerHit = nbt.contains("speedIncreasePerHit")
                ? nbt.getFloat("speedIncreasePerHit")
                : (float) Overgeared.getConfig().POOR_ARROW_SPEED_INCREASE();

        movingRight = !nbt.contains("movingRight") || nbt.getBoolean("movingRight");

        perfectZoneStart = nbt.contains("perfectZoneStart") ? nbt.getInt("perfectZoneStart") : (100 - Overgeared.getConfig().POOR_ZONE_STARTING_SIZE()) / 2;
        perfectZoneEnd = nbt.contains("perfectZoneEnd") ? nbt.getInt("perfectZoneEnd") : (100 + Overgeared.getConfig().POOR_ZONE_STARTING_SIZE()) / 2;
        goodZoneStart = nbt.contains("goodZoneStart") ? nbt.getInt("goodZoneStart") : Mth.clamp(perfectZoneStart - 20, 0, 100);
        goodZoneEnd = nbt.contains("goodZoneEnd") ? nbt.getInt("goodZoneEnd") : Mth.clamp(perfectZoneEnd + 20, goodZoneStart, 100);

        if (nbt.contains("zoneShrinkFactor")) {
            zoneShrinkFactor = nbt.getFloat("zoneShrinkFactor");
        }
        if (nbt.contains("zoneShiftAmount")) {
            zoneShiftAmount = nbt.getFloat("zoneShiftAmount");
        }

        // Clamp values
        arrowSpeed = Math.min(arrowSpeed, maxArrowSpeed);
        arrowPosition = Mth.clamp(arrowPosition, 0f, 100f);
        perfectZoneStart = Mth.clamp(perfectZoneStart, 0, 100);
        perfectZoneEnd = Mth.clamp(perfectZoneEnd, perfectZoneStart, 100);
        goodZoneStart = Mth.clamp(goodZoneStart, 0, 100);
        goodZoneEnd = Mth.clamp(goodZoneEnd, goodZoneStart, 100);
    }

    public static UUID getOccupiedAnvil(BlockPos pos) {
        return occupiedAnvils.get(pos);
    }

    public static void putOccupiedAnvil(BlockPos pos, UUID me) {
        occupiedAnvils.put(pos, me);
    }

    private static BlockPos pendingMinigamePos = null;

    public static void setPendingMinigame(BlockPos pos) {
        pendingMinigamePos = pos;
    }

    public static BlockPos getPendingMinigamePos() {
        return pendingMinigamePos;
    }

    public static void clearPendingMinigame() {
        pendingMinigamePos = null;
    }

    public static int getSkillLevel() {
        return skillLevel;
    }

    public static void setupForQuality(String quality) {
        switch (quality.toLowerCase(java.util.Locale.ROOT)) {
            case "none" -> {
                arrowSpeed = (float) Overgeared.getConfig().DEFAULT_ARROW_SPEED();
                speedIncreasePerHit = (float) Overgeared.getConfig().DEFAULT_ARROW_SPEED_INCREASE();
                maxArrowSpeed = (float) Overgeared.getConfig().DEFAULT_MAX_ARROW_SPEED();
                zoneShrinkFactor = (float) Overgeared.getConfig().DEFAULT_ZONE_SHRINK_FACTOR();
                perfectZoneStart = (100 - Overgeared.getConfig().DEFAULT_ZONE_STARTING_SIZE()) / 2;
                perfectZoneEnd = (100 + Overgeared.getConfig().DEFAULT_ZONE_STARTING_SIZE()) / 2;
                goodZoneStart = Math.max((100 - Overgeared.getConfig().DEFAULT_ZONE_STARTING_SIZE() * 3) / 2, 1);
                goodZoneEnd = Math.min((100 + Overgeared.getConfig().DEFAULT_ZONE_STARTING_SIZE() * 3) / 2, 100);
                minPerfectSize = Overgeared.getConfig().DEFAULT_MIN_PERFECT_ZONE();
                perfectZoneSize = perfectZoneEnd - perfectZoneStart;
            }
            case "master" -> {
                arrowSpeed = (float) Overgeared.getConfig().MASTER_ARROW_SPEED();
                speedIncreasePerHit = (float) Overgeared.getConfig().MASTER_ARROW_SPEED_INCREASE();
                maxArrowSpeed = (float) Overgeared.getConfig().MASTER_MAX_ARROW_SPEED();
                zoneShrinkFactor = (float) Overgeared.getConfig().MASTER_ZONE_SHRINK_FACTOR();
                perfectZoneStart = (100 - Overgeared.getConfig().MASTER_ZONE_STARTING_SIZE()) / 2;
                perfectZoneEnd = (100 + Overgeared.getConfig().MASTER_ZONE_STARTING_SIZE()) / 2;
                goodZoneStart = Math.max((100 - Overgeared.getConfig().MASTER_ZONE_STARTING_SIZE() * 3) / 2, 1);
                goodZoneEnd = Math.min((100 + Overgeared.getConfig().MASTER_ZONE_STARTING_SIZE() * 3) / 2, 100);
                minPerfectSize = Overgeared.getConfig().MASTER_MIN_PERFECT_ZONE();
                perfectZoneSize = perfectZoneEnd - perfectZoneStart;
            }
            case "perfect" -> {
                arrowSpeed = (float) Overgeared.getConfig().PERFECT_ARROW_SPEED();
                speedIncreasePerHit = (float) Overgeared.getConfig().PERFECT_ARROW_SPEED_INCREASE();
                maxArrowSpeed = (float) Overgeared.getConfig().PERFECT_MAX_ARROW_SPEED();
                zoneShrinkFactor = (float) Overgeared.getConfig().PERFECT_ZONE_SHRINK_FACTOR();
                perfectZoneStart = (100 - Overgeared.getConfig().PERFECT_ZONE_STARTING_SIZE()) / 2;
                perfectZoneEnd = (100 + Overgeared.getConfig().PERFECT_ZONE_STARTING_SIZE()) / 2;
                goodZoneStart = Math.max((100 - Overgeared.getConfig().PERFECT_ZONE_STARTING_SIZE() * 3) / 2, 1);
                goodZoneEnd = Math.min((100 + Overgeared.getConfig().PERFECT_ZONE_STARTING_SIZE() * 3) / 2, 100);
                minPerfectSize = Overgeared.getConfig().PERFECT_MIN_PERFECT_ZONE();
                perfectZoneSize = perfectZoneEnd - perfectZoneStart;
            }
            case "expert" -> {
                arrowSpeed = (float) Overgeared.getConfig().EXPERT_ARROW_SPEED();
                speedIncreasePerHit = (float) Overgeared.getConfig().EXPERT_ARROW_SPEED_INCREASE();
                maxArrowSpeed = (float) Overgeared.getConfig().EXPERT_MAX_ARROW_SPEED();
                zoneShrinkFactor = (float) Overgeared.getConfig().EXPERT_ZONE_SHRINK_FACTOR();
                perfectZoneStart = (100 - Overgeared.getConfig().EXPERT_ZONE_STARTING_SIZE()) / 2;
                perfectZoneEnd = (100 + Overgeared.getConfig().EXPERT_ZONE_STARTING_SIZE()) / 2;
                goodZoneStart = Math.max((100 - Overgeared.getConfig().EXPERT_ZONE_STARTING_SIZE() * 3) / 2, 1);
                goodZoneEnd = Math.min((100 + Overgeared.getConfig().EXPERT_ZONE_STARTING_SIZE() * 3) / 2, 100);
                minPerfectSize = Overgeared.getConfig().EXPERT_MIN_PERFECT_ZONE();
                perfectZoneSize = perfectZoneEnd - perfectZoneStart;
            }
            case "well" -> {
                arrowSpeed = (float) Overgeared.getConfig().WELL_ARROW_SPEED();
                speedIncreasePerHit = (float) Overgeared.getConfig().WELL_ARROW_SPEED_INCREASE();
                maxArrowSpeed = (float) Overgeared.getConfig().WELL_MAX_ARROW_SPEED();
                zoneShrinkFactor = (float) Overgeared.getConfig().WELL_ZONE_SHRINK_FACTOR();
                perfectZoneStart = (100 - Overgeared.getConfig().WELL_ZONE_STARTING_SIZE()) / 2;
                perfectZoneEnd = (100 + Overgeared.getConfig().WELL_ZONE_STARTING_SIZE()) / 2;
                goodZoneStart = Math.max((100 - Overgeared.getConfig().WELL_ZONE_STARTING_SIZE() * 3) / 2, 1);
                goodZoneEnd = Math.min((100 + Overgeared.getConfig().WELL_ZONE_STARTING_SIZE() * 3) / 2, 100);
                minPerfectSize = Overgeared.getConfig().WELL_MIN_PERFECT_ZONE();
                perfectZoneSize = perfectZoneEnd - perfectZoneStart;
            }
            default -> { // poor
                arrowSpeed = (float) Overgeared.getConfig().POOR_ARROW_SPEED();
                speedIncreasePerHit = (float) Overgeared.getConfig().POOR_ARROW_SPEED_INCREASE();
                maxArrowSpeed = (float) Overgeared.getConfig().POOR_MAX_ARROW_SPEED();
                zoneShrinkFactor = (float) Overgeared.getConfig().POOR_ZONE_SHRINK_FACTOR();
                perfectZoneStart = (100 - Overgeared.getConfig().POOR_ZONE_STARTING_SIZE()) / 2;
                perfectZoneEnd = (100 + Overgeared.getConfig().POOR_ZONE_STARTING_SIZE()) / 2;
                goodZoneStart = Math.max((100 - Overgeared.getConfig().POOR_ZONE_STARTING_SIZE() * 3) / 2, 1);
                goodZoneEnd = Math.min((100 + Overgeared.getConfig().POOR_ZONE_STARTING_SIZE() * 3) / 2, 100);
                minPerfectSize = Overgeared.getConfig().POOR_MIN_PERFECT_ZONE();
                perfectZoneSize = perfectZoneEnd - perfectZoneStart;
            }
        }
    }

    // ===============================
    // Popup class
    // ===============================
    public static class Popup {
        public final Component text;
        public float age; // milliseconds

        public Popup(Component text) {
            this.text = text;
            this.age = 0f;
        }
    }
}
