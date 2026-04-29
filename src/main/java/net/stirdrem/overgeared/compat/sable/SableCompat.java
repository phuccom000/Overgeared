package net.stirdrem.overgeared.compat.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SableCompat {
    public static double distanceSquaredWithSubLevels(Level level, Vec3 a, Vec3 b) {
        return SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, a, b);
    }
}
