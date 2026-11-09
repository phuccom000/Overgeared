package net.stirdrem.overgeared.compat.sable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Sable sub-level support. Sable has no 26.3 build yet, so this always behaves as if Sable
 * is absent: distances and water checks only look at the main level. Callers already gate on
 * {@link #LOADED}; restore the SableCompanion-backed version once Sable is updated.
 */
public class SableCompat {
    public static final String MOD_ID = "sable";

    public static final boolean LOADED = false;

    public static double distanceSquaredWithSubLevels(Level level, Vec3 a, Vec3 b) {
        return a.distanceToSqr(b);
    }

    public static boolean isWater(Level level, Position pos) {
        var state = level.getBlockState(BlockPos.containing(pos));
        return state.is(Blocks.WATER) || state.is(Blocks.WATER_CAULDRON);
    }
}
