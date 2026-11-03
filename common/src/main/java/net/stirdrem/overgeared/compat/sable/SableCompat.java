package net.stirdrem.overgeared.compat.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.platform.Services;

public class SableCompat {
    /**
     * Sable mod ID constant
     */
    public static final String MOD_ID = "sable";

    /**
     * Whether Sable is loaded
     */
    public static final boolean LOADED = Services.PLATFORM.isModLoaded(MOD_ID) && companionPresent();

    private static boolean companionPresent() {
        // The companion library is only bundled with the NeoForge jar
        try {
            Class.forName("dev.ryanhcode.sable.companion.SableCompanion", false, SableCompat.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

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
