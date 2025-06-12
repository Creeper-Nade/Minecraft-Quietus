package com.minecraftquietus.quietus.entity.projectiles;

import java.util.function.Function;

import com.minecraftquietus.quietus.item.property.WeaponProjectileProperty;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


/**
 * Simple straight travelling projectile with gravity, and does effect on entity via contact as a hit
 */
public abstract class QuietusProjectile extends Projectile {
    // default values
    protected float gravity = 0.05f;
    protected float knockback = 0.4f;
    protected float baseDamage = 5.0f;
    protected int persistanceTicks = 200;
    protected double critChance = 0.05d;
    protected Function<Float,Float> critDamageOperation = (damage) -> (float)(damage*(1.0d+0.5d));

    public QuietusProjectile(EntityType<? extends QuietusProjectile> type, Level level) {
        super(type, level);
    }
    
    public void configure(WeaponProjectileProperty projectileProperty) {
        this.gravity = projectileProperty.gravity();
        this.setNoGravity(gravity == 0.0);
        this.knockback = projectileProperty.knockback();
        this.baseDamage = projectileProperty.damage();
        this.persistanceTicks = projectileProperty.persistanceTicks();
        this.critChance = projectileProperty.critChance();
        this.critDamageOperation = projectileProperty.critOperation();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isNoGravity()) {
            applyGravity();

        }
        this.applyInertia();
        this.spawnTrailParticles();

        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

        if ((hitresult.getType() == HitResult.Type.BLOCK) && !level().isClientSide) {
            this.onHitBlock((BlockHitResult) hitresult);
            // Creepernade: use discard Action to discard so that it triggers particles and stuffs
            discardAction();
            return;
        }
        if (hitresult.getType() == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) hitresult);
        }
        //ProjectileUtil.rotateTowardsMovement(this, 0.2F);
        //if(level().isClientSide) this.applyInteria();

        this.setPos(this.position().add(this.getDeltaMovement()));
        this.updateRotation();
        if(this.tickCount> this.persistanceTicks) {
            discardAction();
        }


    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide && result.getEntity() != this.getOwner() &&(result.getEntity() instanceof LivingEntity || result.getEntity() instanceof EndCrystal) && this.getOwner() instanceof LivingEntity livingOwner) {
            boolean crit = this.makeCrit();
            float damage = this.calculateDamage(crit, baseDamage);
                this.applyImpactEffects(result.getEntity(), damage, crit, livingOwner);
            discardAction();
        }
    }
    
    private boolean makeCrit()
    {
        return this.random.nextDouble() < getCritChance();
    }

    /**
     * Calculate final damage that should be done. Calls critDamageOperation and apply to base damage if crit happens to chance
     * @param isCrit boolean whether or not is crit
     * @param initialDamage float value to calculate with
     * @return final damage calculated
     */
    public float calculateDamage(boolean isCrit, float initialDamage)
    {
        float damage = initialDamage;
        if (isCrit)
        {
            damage = this.critDamageOperation.apply(initialDamage);
        }
        //add some other additional crit dmg here


        return damage;
    }
    public double getCritChance()
    {
        //reserved for addition of crit chance
        return critChance;
    }
    public float knockbackMultiplier()
    {
        //reserved for addition of knockback, such as enchantment
        return 0;
    }

    protected void applyImpactEffects(Entity Target, float damage, boolean is_crit, LivingEntity livingOwner) {

            Vec3 pos = Target.position();
            if (Target.level() instanceof ServerLevel serverLevel) {
                Target.hurtServer(serverLevel, damageSources().mobProjectile(this,livingOwner), damage);
                if(Target instanceof LivingEntity livingTarget)
                applyKnockback(livingTarget);
                if(is_crit) ((ServerLevel)this.level()).sendParticles(ParticleTypes.CRIT,pos.x, pos.y,pos.z, 50, 0,0.5,0,0.5);
            }

    }

    protected void discardAction(){
        this.spawnImpactParticles();
        this.discard();
    }

    protected void applyKnockback(LivingEntity target) {
        float finalKnockback = knockback *(1+ knockbackMultiplier());
        Vec3 knockbackVec = this.getDeltaMovement().normalize().scale(finalKnockback);
        target.knockback(finalKnockback,
                -knockbackVec.x,
                -knockbackVec.z);
    }


    private void applyInertia() {
        Vec3 velocity = this.getDeltaMovement();
        Vec3 pos = this.position();
        float f = 1;
        if (this.isInWater()) {
            for(int i = 0; i < 4; ++i) {
                //float f1 = 0.25F;
                this.level().addParticle(ParticleTypes.BUBBLE, pos.x - velocity.x * (double)0.25F, pos.y - velocity.y * (double)0.25F, pos.z - velocity.z * (double)0.25F, velocity.x, velocity.y, velocity.z);
            }
            if(!this.isNoGravity()) f = 0.9F;
        } else if(!this.isNoGravity()) {
            f = 0.99F;
        }
        else f=1;

        this.setDeltaMovement(velocity.scale((double)f));
    }

    @Override
    protected double getDefaultGravity() {
        return (double)gravity;
    }

    protected abstract void spawnImpactParticles();
    protected abstract void spawnTrailParticles();
}



/*
package com.minecraftquietus.quietus.entity.projectiles.magic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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

import java.util.Random;

public abstract class MagicalProjectile extends Projectile {
    protected float gravity = 0.05f;
    protected float knockback = 0.4f;
    protected float base_damage = 5.0f;
    protected int lifespan = 200;
    protected double crit_chance;
    protected double crit_dmg=0.5;

    public MagicalProjectile(EntityType<? extends MagicalProjectile> type, Level level) {
        super(type, level);
    }

    public void ConfigProjectile(float gravity, float knockback, float base_damage, int lifespan,double base_crit_chance)
    {
        this.gravity=gravity;
        setNoGravity(gravity <= 0.0);
        this.knockback=knockback;
        this.base_damage=base_damage;
        this.lifespan=lifespan;
        this.crit_chance=base_crit_chance;
    }

    @Override
    public void tick() {

        super.tick();
        if (!this.isNoGravity()) {
            applyGravity();

        }
        applyInertia();
        spawnTrailParticles();

        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);


        if ((hitresult.getType() == HitResult.Type.BLOCK) && !level().isClientSide) {
            onHitBlock((BlockHitResult) hitresult);
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
        if(tickCount>lifespan) DiscardAction();


    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide && result.getEntity() != getOwner() &&result.getEntity() instanceof LivingEntity livingEntity ) {
            boolean is_crit= crit_checker();
            float damage= base_damage * (1+ ManaAtkMultiplier(is_crit));
                applyImpactEffects(livingEntity, damage,is_crit);
            DiscardAction();
        }
    }
    private boolean crit_checker()
    {
        Random r=new Random();
        return r.nextDouble() <= getCrit_chance();
    }


    public float ManaAtkMultiplier(boolean is_crit)
    {
        float multiplier_total=0.0f;

        if(is_crit)
        {
            //crit dmg is a percentage, 1= 100%, 0.5= 50%
            multiplier_total += (float) crit_dmg;
        }
        //add some other additional mana dmg here


        return multiplier_total;
    }
    public double getCrit_chance()
    {
        //crit chance is a percentage, 1= 100%, 0.5= 50%
        //reserved for addition of crit chance
        return crit_chance;
    }
    public float KBMultiplier()
    {
        //reserved for addition of knockback, such as enchantment
        return 0;
    }

    protected void applyImpactEffects(LivingEntity livingTarget, float damage, boolean is_crit) {

            Vec3 vec31 = livingTarget.position();
            livingTarget.hurt(damageSources().indirectMagic(this,getOwner()), damage);
            applyKnockback(livingTarget);
            if(is_crit) ((ServerLevel)this.level()).sendParticles(ParticleTypes.CRIT,vec31.x, vec31.y,vec31.z, 50, 0,0.5,0,0.5);

    }
    protected void DiscardAction(){
        spawnImpactParticles();
        discard();
    }

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
        float f = 1;
        if (this.isInWater()) {
            for(int i = 0; i < 4; ++i) {
                float f1 = 0.25F;
                this.level().addParticle(ParticleTypes.BUBBLE, vec31.x - vec3.x * (double)0.25F, vec31.y - vec3.y * (double)0.25F, vec31.z - vec3.z * (double)0.25F, vec3.x, vec3.y, vec3.z);
            }
            if(!this.isNoGravity()) f = 0.9F;
        } else if(!this.isNoGravity()) {
            f = 0.99F;
        }
        else f=1;

        this.setDeltaMovement(vec3.scale((double)f));
    }

    @Override
    protected double getDefaultGravity() {
        return (double)gravity;
    }

    protected abstract void spawnImpactParticles();
    protected abstract void spawnTrailParticles();
}


 */