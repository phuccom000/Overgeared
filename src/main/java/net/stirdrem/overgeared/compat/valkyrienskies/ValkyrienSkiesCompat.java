package net.stirdrem.overgeared.compat.valkyrienskies;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * The original Forge version compiles directly against Valkyrien Skies' API jar
 * (org.valkyrienskies.mod.common.VSGameUtilsKt) to transform a block position by the ship's
 * transform if it sits on a moving ship. That dependency isn't wired into this Fabric project
 * (no VS Fabric artifact/repository configured), so this is a stub that always returns the
 * vanilla block-center position. Callers already gate on
 * FabricLoader.getInstance().isModLoaded("valkyrienskies") before reaching here, so this only
 * affects anvil-distance checks for players with Valkyrien Skies installed who build on a ship -
 * the check still runs, it just measures against the ship's local coordinate instead of its
 * transformed world position. Wiring up real ship-transform support needs the VS Fabric API
 * dependency added to build.gradle plus the equivalent of VSGameUtilsKt.getShipManagingPos.
 */
public final class ValkyrienSkiesCompat {

    private ValkyrienSkiesCompat() {
    }

    public static Vec3d getActualWorldPos(ServerWorld world, BlockPos blockPos) {
        return Vec3d.ofCenter(blockPos);
    }
}
