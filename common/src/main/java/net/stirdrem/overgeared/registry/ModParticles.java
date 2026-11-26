package net.stirdrem.overgeared.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public class ModParticles {
    private static final RegistrationProvider<ParticleType<?>> PARTICLE_TYPES = RegistrationProvider.get(Registries.PARTICLE_TYPE);

    // SimpleParticleType's constructor is protected in 1.20.1, hence the anonymous subclass
    public static final RegistryEntry<SimpleParticleType> SPARK = PARTICLE_TYPES.register("spark", () -> new SimpleParticleType(false) {});

    /** Class-loads this registry. Call from the loader entrypoint. */
    public static void init() {
    }
}
