package com.minecraftquietus.quietus.entity.projectiles.magic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class MagicalProjectile extends Projectile {
    protected float gravity = 0.05f;
    protected float knockback = 0.4f;
    protected float base_damage = 5.0f;
    protected int lifespan = 200;

    public MagicalProjectile(EntityType<? extends MagicalProjectile> type, Level level) {
        super(type, level);
    }

    public void ConfigProjectile(float gravity, float knockback, float base_damage, int lifespan)
    {
        this.gravity=gravity;
        setNoGravity(gravity <= 0.0);
        this.knockback=knockback;
        this.base_damage=base_damage;
        this.lifespan=lifespan;
    }

    @Override
    public void tick() {

        super.tick();
        if (!this.isNoGravity()) {
            applyGravity();

        }
        applyInertia();

        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);


        if (hitresult.getType() == HitResult.Type.BLOCK || tickCount>lifespan) {
            DiscardAction();
            return;
        }
        if (hitresult.getType() == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult) hitresult);
        }
        //ProjectileUtil.rotateTowardsMovement(this, 0.2F);
        //if(level().isClientSide) applyMotion();



        setPos(this.position().add(this.getDeltaMovement()));
        this.updateRotation();



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
    public float KBMultiplier()
    {
        return 0;
    }

    protected void applyImpactEffects(Entity target, float damage) {
        if (target instanceof LivingEntity livingTarget) {
            livingTarget.hurt(damageSources().indirectMagic(getOwner(),this), damage);
            applyKnockback(livingTarget);
        }
    }
    protected void DiscardAction(){ discard();}

    protected void applyKnockback(LivingEntity target) {
        float finalKnockback= knockback *(1+ KBMultiplier());
        Vec3 knockbackVec = this.getDeltaMovement().normalize().scale(finalKnockback);
        target.knockback(finalKnockback,
                -knockbackVec.x,
                -knockbackVec.z);
    }


    private void applyInertia() {
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 vec31 = this.position();
        float f;
        if (this.isInWater()) {
            for(int i = 0; i < 4; ++i) {
                float f1 = 0.25F;
                this.level().addParticle(ParticleTypes.BUBBLE, vec31.x - vec3.x * (double)0.25F, vec31.y - vec3.y * (double)0.25F, vec31.z - vec3.z * (double)0.25F, vec3.x, vec3.y, vec3.z);
            }

            f = 0.9F;
        } else {
            f = 0.99F;
        }

        this.setDeltaMovement(vec3.scale((double)f));
    }

    @Override
    protected double getDefaultGravity() {
        return (double)gravity;
    }

    protected abstract void spawnImpactParticles();
}

