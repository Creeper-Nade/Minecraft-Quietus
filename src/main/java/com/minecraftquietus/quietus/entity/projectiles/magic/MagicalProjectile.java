package com.minecraftquietus.quietus.entity.projectiles.magic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class MagicalProjectile extends Projectile {
    protected float gravity = 0.05f;
    protected float knockback = 0.4f;
    protected float initialVelocity = 1.0f;
    protected float base_damage = 5.0f;
    protected int lifespan = 200;

    public MagicalProjectile(EntityType<? extends MagicalProjectile> type, Level level) {
        super(type, level);
    }

    public void ConfigProjectile(float gravity, float knockback, float velocity, float base_damage, int lifespan)
    {
        this.gravity=gravity;
        this.knockback=knockback;
        this.initialVelocity=velocity;
        this.base_damage=base_damage;
        this.lifespan=lifespan;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -gravity, 0.0D));
        }
        if(tickCount>lifespan) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide && result.getEntity() != getOwner()) {
            float damage= base_damage * (1+ ManaAtkMultiplier());
                applyImpactEffects(result.getEntity(), damage);
            spawnImpactParticles();
            discard();
        }
    }

    public float ManaAtkMultiplier()
    {
        return 0;
    }

    protected void applyImpactEffects(Entity target, float damage) {
        if (target instanceof LivingEntity livingTarget) {
            livingTarget.hurt(damageSources().indirectMagic(getOwner(),this), damage);
            applyKnockback(livingTarget);
        }
    }

    protected void applyKnockback(LivingEntity target) {
        Vec3 knockbackVec = this.getDeltaMovement().normalize().scale(knockback);
        target.knockback(knockback,
                knockbackVec.x,
                knockbackVec.z);
    }

    protected abstract void spawnImpactParticles();
}

