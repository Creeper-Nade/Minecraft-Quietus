package com.minecraftquietus.quietus.entity.projectiles;

import java.util.List;
import java.util.function.Function;

import com.minecraftquietus.quietus.item.property.WeaponProjectileProperty;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
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

    protected static final String NBT_TAG_PROJECTILE_GRAVITY = "ProjectileGravity";
    protected static final EntityDataAccessor<Float> DATA_PROJECTILE_GRAVITY_ID = SynchedEntityData.defineId(QuietusProjectile.class, EntityDataSerializers.FLOAT);

    public QuietusProjectile(EntityType<? extends QuietusProjectile> type, Level level) {
        super(type, level);
    }
    
    public void configure(WeaponProjectileProperty projectileProperty) {
        this.gravity = projectileProperty.gravity();
        this.getEntityData().set(DATA_PROJECTILE_GRAVITY_ID, projectileProperty.gravity());
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

    protected abstract void applyImpactEffects(Entity Target, float damage, boolean is_crit, LivingEntity livingOwner);

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

    public float getSynchedGravity() {
        return this.getEntityData().get(DATA_PROJECTILE_GRAVITY_ID);
    }

    @Override
    protected double getDefaultGravity() {
        return (double)this.getSynchedGravity();
    }
    @Override
    public void addAdditionalSaveData(CompoundTag content) {
        super.addAdditionalSaveData(content);
        content.putFloat(NBT_TAG_PROJECTILE_GRAVITY, this.gravity);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag content) {
        super.readAdditionalSaveData(content);
        float r = content.getFloatOr(NBT_TAG_PROJECTILE_GRAVITY, 0.05f);
        this.gravity = r;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_PROJECTILE_GRAVITY_ID, 0.05f);
    }
    
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_PROJECTILE_GRAVITY_ID.equals(key)) {
            this.gravity = getSynchedGravity();
        }
    } 

    protected abstract void spawnImpactParticles();
    protected abstract void spawnTrailParticles();
}


