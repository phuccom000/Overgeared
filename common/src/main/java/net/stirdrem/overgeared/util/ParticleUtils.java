package net.stirdrem.overgeared.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.registry.ModParticles;

public class ParticleUtils {
    public static void spawnAnvilParticles(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        RandomSource random = level.getRandom();
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.05;
        double z = pos.getZ() + 0.5;
        for (int i = 0; i < 10; i++) {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float speed = 0.12F + random.nextFloat() * 0.18F;
            // count 0 makes sendParticles use the offset arguments as this spark's velocity
            serverLevel.sendParticles(ModParticles.SPARK.get(), x, y, z, 0,
                    Mth.cos(angle) * speed, 0.15 + random.nextFloat() * 0.2, Mth.sin(angle) * speed, 1.0);
        }
    }
}
