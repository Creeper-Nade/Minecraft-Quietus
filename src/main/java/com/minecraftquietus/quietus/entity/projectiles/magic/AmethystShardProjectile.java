package com.minecraftquietus.quietus.entity.projectiles.magic;

import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectile;
import com.minecraftquietus.quietus.item.property.QuietusProjectileProperty;
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
    protected void applyImpactEffects(Entity target, float damage, boolean is_crit, Entity owner) {
            if(owner instanceof LivingEntity livingOwner)
            {
                Vec3 pos = target.position();
                //DamageSource damageSource = damageSources().mobProjectile(this, livingOwner);
                DamageSource damageSource = new DamageSource(
                        // The damage type holder to use. Query from the registry. This is the only required parameter.
                        this.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(QuietusDamageType.MAGIC_PROJECTILE_DAMAGE),
                        // The direct entity. For example, if a skeleton shot you, the skeleton would be the causing entity
                        // (= the parameter above), and the arrow would be the direct entity (= this parameter). Similar to
                        // the causing entity, this isn't always applicable and therefore nullable. Optional, defaults to null.
                        this,
                        // The entity causing the damage. This isn't always applicable (e.g. when falling out of the world)
                        // and may therefore be null. Optional, defaults to null.
                        owner,
                        // The damage source position. This is rarely used, one example would be intentional game design
                        // (= nether beds exploding). Nullable and optional, defaulting to null.
                        null
                );
                if (target.level() instanceof ServerLevel serverLevel) {
                    target.hurtServer(serverLevel, damageSource, damage);
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
                // The direct entity. For example, if a skeleton shot you, the skeleton would be the causing entity
                // (= the parameter above), and the arrow would be the direct entity (= this parameter). Similar to
                // the causing entity, this isn't always applicable and therefore nullable. Optional, defaults to null.
                this,
                // The entity causing the damage. This isn't always applicable (e.g. when falling out of the world)
                // and may therefore be null. Optional, defaults to null.
                owner,
                // The damage source position. This is rarely used, one example would be intentional game design
                // (= nether beds exploding). Nullable and optional, defaulting to null.
                null
        );
        /*
        if(owner instanceof  LivingEntity livingEntity)
        return damageSources().mobProjectile(this,livingEntity);
        else
            return damageSources().magic();*/
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }
}
