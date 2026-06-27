package gg.sheepish.botanypotsadditions.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class SprinklerWaterParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    SprinklerWaterParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.quadSize = 0.034F + random.nextFloat() * 0.01F;
        this.lifetime = 20 + random.nextInt(6);
        this.gravity = 0F;
        this.friction = 1F;
        this.hasPhysics = false;
        this.setParticleSpeed(xSpeed, ySpeed, zSpeed);
        this.setColor(0.08F, 0.28F, 0.95F);
        this.setAlpha(0.95F);
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        if (yd > -0.022D) {
            yd = -0.022D;
        }

        super.tick();
        setSpriteFromAge(sprites);

        float life = (float) age / (float) lifetime;
        setAlpha(0.95F * (1F - life));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SprinklerWaterParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
