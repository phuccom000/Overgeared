package net.stirdrem.overgeared.compat.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

public class SableCompat {
    /**
     * Sable mod ID constant
     */
    public static final String MOD_ID = "sable";

    /**
     * Whether Sable is loaded
     */
    public static final boolean LOADED = ModList.get().isLoaded(MOD_ID);

    public static double distanceSquaredWithSubLevels(Level level, Vec3 a, Vec3 b) {
        return SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, a, b);
    }

    public static boolean isWater(Level level, Position pos) {
        var companion = SableCompanion.INSTANCE;

        return companion.findIncludingSubLevels(
                level,
                pos,
                true,
                companion.getContaining(level, pos),
                (sub, checkPos) -> {
                    var state = level.getBlockState(checkPos);
                    return state.is(Blocks.WATER) || state.is(Blocks.WATER_CAULDRON);
                }
        );

    }
}
