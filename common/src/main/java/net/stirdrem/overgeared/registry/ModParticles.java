package net.stirdrem.overgeared.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.stirdrem.overgeared.OvergearedMod;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, OvergearedMod.MOD_ID);

    // SimpleParticleType's constructor is protected in 1.21.1, hence the anonymous subclass
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPARK =
            PARTICLE_TYPES.register("spark", () -> new SimpleParticleType(false) {});

    public static void register() {
        PARTICLE_TYPES.register();
    }
}
