package net.stirdrem.overgeared.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class SparkParticle extends SimpleAnimatedParticle {
    private static final int WHITE = 0xFFFFFF;
    private static final int ASH_GRAY = 0x737373;

    private static final float HOT_R = 1.0F;
    private static final float HOT_G = 0.55F;
    private static final float HOT_B = 0.05F;
    // firework sparks drift 0.2/tick over ~50 ticks; ours live 8-15, so drift faster to actually reach orange
    private static final float HOT_DRIFT = 0.4F;

    protected SparkParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, sprites, 0.9F);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.friction = 0.92F;
        this.quadSize = 0.06F + this.random.nextFloat() * 0.04F;
        this.lifetime = 8 + this.random.nextInt(8);
        this.setColor(WHITE);
        this.setFadeColor(ASH_GRAY);
        this.setSpriteFromAge(sprites);
    }

    // first half heats white -> orange; the second half is vanilla's fade toward the fade color with alpha falloff
    @Override
    public void tick() {
        super.tick();
        if (this.age > this.lifetime / 2) return;
        this.rCol += (HOT_R - this.rCol) * HOT_DRIFT;
        this.gCol += (HOT_G - this.gCol) * HOT_DRIFT;
        this.bCol += (HOT_B - this.bCol) * HOT_DRIFT;
    }

    @Override
    public float getQuadSize(float partialTick) {
        float t = (this.age + partialTick) / this.lifetime;
        return this.quadSize * (1.0F - t * t * 0.5F);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new SparkParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
