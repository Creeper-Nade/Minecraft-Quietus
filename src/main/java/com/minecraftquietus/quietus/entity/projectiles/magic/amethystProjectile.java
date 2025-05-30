package com.minecraftquietus.quietus.entity.projectiles.magic;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class amethystProjectile extends MagicalProjectile {
    public amethystProjectile(EntityType<? extends MagicalProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void spawnImpactParticles() {
        Vec3 vec31 = this.position();
        ((ServerLevel)this.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_CLUSTER.defaultBlockState()),vec31.x, vec31.y,vec31.z, 30, 0,0,0,0.5);
        this.level().playSound(null, vec31.x, vec31.y, vec31.z,
                SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

        //level().addParticle(ParticleTypes.WITCH,
        //        getX(), getY(), getZ(), 0, 0, 0);
    }

    @Override
    protected void spawnTrailParticles()
    {
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 vec31 = this.position();

        for(int i = 0; i < 2; ++i) {
            float f1 = 0.05F;
            this.level().addParticle(ParticleTypes.WITCH,vec31.x - vec3.x * (double)f1, vec31.y - vec3.y * (double)f1, vec31.z - vec3.z * (double)f1, vec3.x, vec3.y, vec3.z);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }
}
