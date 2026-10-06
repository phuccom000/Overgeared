package net.stirdrem.overgeared.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.config.ServerConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientAnvilMinigameData {
    public static UUID ownerUUID = null;
    private static boolean isVisible = false;
    public static boolean minigameStarted = false;
    public static ItemStack resultItem = null;
    public static int hitsRemaining = 0;
    public static float arrowPosition = 0;
    public static float arrowSpeed = ServerConfig.POOR_ARROW_SPEED.get().floatValue();
    public static final float maxArrowSpeed = ServerConfig.POOR_MAX_ARROW_SPEED.get().floatValue();
    public static float speedIncreasePerHit = ServerConfig.POOR_ARROW_SPEED_INCREASE.get().floatValue();
    public static boolean movingRight = true;
    public static int perfectHits = 0;
    public static int goodHits = 0;
    public static int missedHits = 0;
    public static int perfectZoneStart = (100 - ServerConfig.POOR_ZONE_STARTING_SIZE.get()) / 2;
    public static int perfectZoneEnd = (100 + ServerConfig.POOR_ZONE_STARTING_SIZE.get()) / 2;
    public static int goodZoneStart = perfectZoneStart - 10;
    public static int goodZoneEnd = perfectZoneEnd + 10;
    public static float zoneShrinkFactor = 0.80f;
    public static float zoneShiftAmount = 15.0f;
    public static Map<BlockPos, UUID> occupiedAnvils = Collections.synchronizedMap(new HashMap<>());
    public static int skillLevel = 0;

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

    public static void loadFromNbt(CompoundTag nbt) {
        isVisible = nbt.getBooleanOr("isVisible", false);

        ownerUUID = nbt.read("ownerUUID", UUIDUtil.CODEC).orElse(null);

        minigameStarted = nbt.getBooleanOr("minigameStarted", false);

        resultItem = ItemStack.EMPTY;
        Minecraft mc = Minecraft.getInstance();
        if (nbt.contains("resultItem") && mc.level != null) {
            resultItem = nbt.read("resultItem", ItemStack.CODEC,
                    mc.level.registryAccess().createSerializationContext(NbtOps.INSTANCE)).orElse(ItemStack.EMPTY);
        }

        hitsRemaining = nbt.getIntOr("hitsRemaining", 0);
        perfectHits = nbt.getIntOr("perfectHits", 0);
        goodHits = nbt.getIntOr("goodHits", 0);
        missedHits = nbt.getIntOr("missedHits", 0);

        arrowPosition = nbt.getFloatOr("arrowPosition", 0f);
        arrowSpeed = nbt.getFloatOr("arrowSpeed", ServerConfig.POOR_ARROW_SPEED.get().floatValue());
        speedIncreasePerHit = nbt.getFloatOr("speedIncreasePerHit", ServerConfig.POOR_ARROW_SPEED_INCREASE.get().floatValue());

        movingRight = nbt.getBooleanOr("movingRight", true);

        perfectZoneStart = nbt.getIntOr("perfectZoneStart", (100 - ServerConfig.POOR_ZONE_STARTING_SIZE.get()) / 2);
        perfectZoneEnd = nbt.getIntOr("perfectZoneEnd", (100 + ServerConfig.POOR_ZONE_STARTING_SIZE.get()) / 2);
        goodZoneStart = nbt.getIntOr("goodZoneStart", Mth.clamp(perfectZoneStart - 20, 0, 100));
        goodZoneEnd = nbt.getIntOr("goodZoneEnd", Mth.clamp(perfectZoneEnd + 20, goodZoneStart, 100));

        zoneShrinkFactor = nbt.getFloatOr("zoneShrinkFactor", zoneShrinkFactor);
        zoneShiftAmount = nbt.getFloatOr("zoneShiftAmount", zoneShiftAmount);

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
}
