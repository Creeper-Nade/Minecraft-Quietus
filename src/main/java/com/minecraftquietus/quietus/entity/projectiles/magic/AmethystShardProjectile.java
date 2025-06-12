package com.minecraftquietus.quietus.entity.projectiles.magic;

import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectile;
import com.minecraftquietus.quietus.item.property.WeaponProjectileProperty;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class AmethystShardProjectile extends QuietusProjectile {
    public AmethystShardProjectile(EntityType<? extends QuietusProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void spawnImpactParticles() {
        if (this.level() instanceof ServerLevel level) {
            Vec3 vec31 = this.position();
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_CLUSTER.defaultBlockState()),vec31.x, vec31.y,vec31.z, 30, 0,0,0,0.5);
            this.level().playSound(null, vec31.x, vec31.y, vec31.z,
                    SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

            //level().addParticle(ParticleTypes.WITCH,
            //        getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void spawnTrailParticles()
    {
        Vec3 velocity = this.getDeltaMovement();
        Vec3 pos = this.position();

        for(int i = 0; i < 2; ++i) {
            float f1 = 0.05F;
            this.level().addParticle(ParticleTypes.WITCH,pos.x - velocity.x * (double)f1, pos.y - velocity.y * (double)f1, pos.z - velocity.z * (double)f1, velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    protected void applyImpactEffects(LivingEntity livingTarget, float damage, boolean is_crit) {
            Vec3 pos = livingTarget.position();
            if (livingTarget.level() instanceof ServerLevel serverLevel) {
                if (this.getOwner() instanceof LivingEntity owner)
                    livingTarget.hurtServer(serverLevel, damageSources().mobProjectile(this, owner), damage);
                applyKnockback(livingTarget);
                if(is_crit) ((ServerLevel)this.level()).sendParticles(ParticleTypes.CRIT,pos.x, pos.y,pos.z, 50, 0,0.5,0,0.5);
            }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }
}
