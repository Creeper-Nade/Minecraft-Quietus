package com.minecraftquietus.quietus.client.particle;

import com.minecraftquietus.quietus.client.particle.particle_options.DustExplosionParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class DustExplosionParticle extends DustParticleBase<DustExplosionParticleOptions> {
    public DustExplosionParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, DustExplosionParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, options, sprites);
        this.xd = this.xd * (double)0.01F + xSpeed;
        this.yd = this.yd * (double)0.01F + ySpeed;
        this.zd = this.zd * (double)0.01F + zSpeed;
        this.x += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.y += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.z += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
    }
    public int getLightColor(float partialTick) {
        // Return a packed light value with maximum block and sky light
        return 0xF000F0;
    }
    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float f = ((float)this.age + scaleFactor) / (float)this.lifetime;
        return this.quadSize * (1.0F - f * f * 0.5F);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<DustExplosionParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(
                DustExplosionParticleOptions type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed,
                RandomSource randomSource
        ) {
            return new DustExplosionParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type, this.sprites);
        }

    }

}
