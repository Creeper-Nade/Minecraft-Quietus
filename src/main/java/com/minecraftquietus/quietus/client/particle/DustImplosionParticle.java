package com.minecraftquietus.quietus.client.particle;

import com.minecraftquietus.quietus.client.particle.particle_options.DustImplosionParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DustParticleBase;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DustImplosionParticle extends DustParticleBase<DustImplosionParticleOptions> {
    private final double xStart;
    private final double yStart;
    private final double zStart;
    private final boolean isGlowing;
    private final Particle.LifetimeAlpha lifetimeAlpha;

    // Add these fields for implosion behavior
    private final double targetX;
    private final double targetY;
    private final double targetZ;

    public DustImplosionParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, boolean isGlowing, Particle.LifetimeAlpha lifetimeAlpha, DustImplosionParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, options, sprites);
        this.isGlowing = isGlowing;
        this.lifetimeAlpha = lifetimeAlpha;
        this.setAlpha(lifetimeAlpha.startAlpha());

        // The implosion target is the spawn point
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;

        // Start particles at their initial positions (based on speed as offset)
        this.xStart = x + xSpeed;
        this.yStart = y + ySpeed;
        this.zStart = z + zSpeed;


        this.xo = this.xStart;
        this.yo = this.yStart;
        this.zo = this.zStart;
        this.x = this.xStart;
        this.y = this.yStart;
        this.z = this.zStart;

        //this.quadSize = 0.1F * (this.random.nextFloat() * this.random.nextFloat() * 3.0F + 1.0F);
        float f = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = f;
        this.gCol = f;
        this.bCol = f;
        this.hasPhysics = false;
        this.lifetime = (int)(Math.random() * 10.0F) + 30;
    }

    public int getLightCoords(float partialTick) {
        if (this.isGlowing) {
            return 0xF000F0;
        } else {
            int i = super.getLightCoords(partialTick);
            float f = (float)this.age / (float)this.lifetime;
            f *= f;
            f *= f;
            int j = i & 255;
            int k = i >> 16 & 255;
            k += (int)(f * 15.0F * 16.0F);
            if (k > 240) {
                k = 240;
            }
            return j | k << 16;
        }
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float progress = (float)this.age / (float)this.lifetime;

        // Add speed multiplier - higher values = faster movement
        float speedMultiplier = 2.0F; // Adjust this value as needed
        float adjustedProgress = Math.min(1.0F, progress * speedMultiplier);

        // Apply easing to the adjusted progress
        float easedProgress = 1 - (1 - adjustedProgress) * (1 - adjustedProgress) * (1 - adjustedProgress);

        // Move particles directly toward center
        this.x = this.xStart + (this.targetX - this.xStart) * easedProgress;
        this.y = this.yStart + (this.targetY - this.yStart) * easedProgress;
        this.z = this.zStart + (this.targetZ - this.zStart) * easedProgress;

        // Check if particle has reached the center
        double currentDx = this.x - this.targetX;
        double currentDy = this.y - this.targetY;
        double currentDz = this.z - this.targetZ;
        double distanceSquared = currentDx * currentDx + currentDy * currentDy + currentDz * currentDz;

        // Remove particle if it's very close to center
        double removalThreshold = 0.05;
        double thresholdSquared = removalThreshold * removalThreshold;

        if (distanceSquared < thresholdSquared) {
            this.remove();
        }
    }

    @Override
    public void extract(QuadParticleRenderState p_324177_, Camera p_323683_, float p_323936_) {
        this.setAlpha(this.lifetimeAlpha.currentAlphaForAge(this.age, this.lifetime, p_323936_));
        super.extract(p_324177_, p_323683_, p_323936_);
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float f = ((float)this.age + scaleFactor) / (float)this.lifetime;
        return this.quadSize * (1.0F - f * f * 0.5F);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<DustImplosionParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(
                DustImplosionParticleOptions type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed,
                RandomSource source
        ) {
            return new DustImplosionParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, true, LifetimeAlpha.ALWAYS_OPAQUE, type, this.sprites);
        }
    }
}