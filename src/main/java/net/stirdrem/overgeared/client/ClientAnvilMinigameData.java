package net.stirdrem.overgeared.client;

import net.minecraft.core.BlockPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side view of which anvils are occupied, plus a pending minigame start.
 * The live minigame state itself lives in {@link net.stirdrem.overgeared.event.AnvilMinigameEvents}.
 */
public class ClientAnvilMinigameData {
    // Synchronized: also written from the integrated server thread in ModItemInteractEvents.releaseAnvil
    private static final Map<BlockPos, UUID> occupiedAnvils = Collections.synchronizedMap(new HashMap<>());
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
}
