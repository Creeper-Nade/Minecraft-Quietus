package com.minecraftquietus.quietus.entity.projectiles.magic;

import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectile;
import com.minecraftquietus.quietus.util.damage.QuietusDamageType;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class SmallAmethystShardProjectile extends QuietusProjectile {
    public SmallAmethystShardProjectile(EntityType<? extends QuietusProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void spawnImpactParticles() {
        if (this.level() instanceof ServerLevel level) {
            Vec3 vec31 = this.position();
            Random random= new Random();
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_CLUSTER.defaultBlockState()),vec31.x, vec31.y,vec31.z, 5, 0,0,0,0.5);
            this.level().playSound(null, vec31.x, vec31.y, vec31.z,
                    SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0F, random.nextFloat(1.0f,3.0f));
        }
    }

    @Override
    protected void spawnTrailParticles()
    {
        Vec3 velocity = this.getDeltaMovement();
        Vec3 pos = this.position();
        float f1 = 0.05F;
        this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState()), pos.x - velocity.x * (double)f1, pos.y - velocity.y * (double)f1, pos.z - velocity.z * (double)f1, velocity.x, velocity.y, velocity.z);
    }
    @Override
    protected void applyImpactEffects(Entity target, float damage, boolean is_crit, Entity owner) {
        if(owner instanceof LivingEntity) {
            Vec3 pos = target.position();
            DamageSource damageSource = new DamageSource(
                    this.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(QuietusDamageType.MAGIC_PROJECTILE_DAMAGE),
                    this,
                    owner,
                    null
            );
            if (target.level() instanceof ServerLevel serverLevel) {
                target.hurtServer(serverLevel,damageSource , damage);
                if (target instanceof LivingEntity livingTarget) applyKnockback(livingTarget,damageSource);
                if(is_crit) ((ServerLevel)this.level()).sendParticles(ParticleTypes.CRIT,pos.x, pos.y,pos.z, 50, 0,0.5,0,0.5);
            }
        }

    }

    @Override
    protected DamageSource getDamageSource(Entity owner)
    {
        return new DamageSource(
                // The damage type holder to use. Query from the registry. This is the only required parameter.
                this.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(QuietusDamageType.MAGIC_PROJECTILE_DAMAGE),
                this,
                owner,
                null
        );
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }
}
