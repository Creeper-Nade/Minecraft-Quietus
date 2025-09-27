package com.minecraftquietus.quietus.client.particle;

import com.minecraftquietus.quietus.client.particle.particle_options.DustExplosionParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class DustExplosion extends DustParticleBase<DustExplosionParticleOptions> {
    public DustExplosion(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, DustExplosionParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, options, sprites);
        this.xd = this.xd * (double)0.01F + xSpeed;
        this.yd = this.yd * (double)0.01F + ySpeed;
        this.zd = this.zd * (double)0.01F + zSpeed;
        this.x += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.y += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.z += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        float f = this.random.nextFloat() * 0.4F + 0.6F;
        Vector3f vector3f = options.getColor();
       // this.rCol = this.randomizeColor(vector3f.x(), f);
        //this.gCol = this.randomizeColor(vector3f.y(), f);
        //this.bCol = this.randomizeColor(vector3f.z(), f);
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
                double zSpeed
        ) {
            return new DustExplosion(level, x, y, z, xSpeed, ySpeed, zSpeed, type, this.sprites);
        }
    }

}
