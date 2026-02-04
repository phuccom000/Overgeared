package net.stirdrem.overgeared.compat.valkyrienskies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import org.joml.Vector3d;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public final class ValkyrienSkiesCompat {

    private ValkyrienSkiesCompat() {
    }

    /**
     * Returns a world position for the given block position, transformed by Valkyrien Skies if the
     * block is on a ship. If the position isn't on a ship or Valkyrien Skies API isn't available,
     * this just returns the original block position as a Vec3.
     */
    public static Vec3 getActualWorldPos(ServerLevel level, BlockPos blockPos) {
        // Try to get the ship managing this block
        var ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
        if (ship == null) {
            // Not on a ship — return vanilla block position
            return Vec3.atCenterOf(blockPos);
        }

        // On a ship — convert local blockpos to JOML vec
        var localJoml = VectorConversionsMCKt.toJOML(Vec3.atCenterOf(blockPos));
        var transform = ship.getTransform().getShipToWorld();

        // Transform ship-local coordinate to world coordinate
        var worldJoml = transform.transformPosition(localJoml, new Vector3d());
        return VectorConversionsMCKt.toMinecraft(worldJoml);
    }
}
