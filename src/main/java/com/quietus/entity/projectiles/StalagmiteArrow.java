package com.quietus.entity.projectiles;

import java.util.Objects;
import java.util.function.Function;

import com.quietus.item.property.QuietusProjectileProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StalagmiteArrow extends AbstractArrow implements IQuietusProjectile {

    public static final int MAX_PIERCE_COUNT = 4;
    public static final double MAX_DIG_DISTANCE = 0.375d;
    public static final double GRAVITY = 0.05d;

    protected static final EntityDataAccessor<Boolean> DATA_MAXIMUM_CRIT_MULTIPLIER_ID =
            SynchedEntityData.defineId(StalagmiteArrow.class, EntityDataSerializers.BOOLEAN);

    protected int piercedCount = 0;
    protected float knockback = 0.4F;
    protected double critChance = 0.05D;
    protected Function<Float, Float> critDamageOperation = damage -> damage * 1.5F;

    public StalagmiteArrow(EntityType<? extends StalagmiteArrow> type, Level level) {
        super(type, level);
    }

    public StalagmiteArrow(Level level, double x, double y, double z, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(QuietusProjectiles.STALAGMITE_ARROW.get(), x, y, z, level, pickupItemStack, firedFromWeapon);
    }

    public StalagmiteArrow(Level level, LivingEntity owner, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(QuietusProjectiles.STALAGMITE_ARROW.get(), owner, level, pickupItemStack, firedFromWeapon);
    }

    @Override
    public void configure(QuietusProjectileProperty projectileProperty, @Nullable ItemStack item) {
        this.setBaseDamage(projectileProperty.damage());
        this.knockback = projectileProperty.knockback();
        this.critChance = projectileProperty.critChance();
        this.critDamageOperation = projectileProperty.critOperation();
        if (item != null && !item.isEmpty()) {
            this.setPickupItemStack(item.copy());
        }
    }

    @Override
    public void setCritical(boolean isCrit) {
        this.setCritArrow(isCrit);
    }

    @Override
    public void rollCriticalHit() {
        this.setCritical(this.random.nextDouble() < this.critChance);
    }

    @Override
    public void applyCastingResult(int successes, int totalChecks) {
        float performance = totalChecks <= 0
                ? 0.0F
                : Math.clamp(successes / (float) totalChecks, 0.0F, 1.0F);
        double baseCritChance = Math.clamp(this.critChance, 0.0D, 1.0D);
        this.critChance = baseCritChance + (1.0D - baseCritChance) * performance;
        float critMultiplier = 1.0F + 0.5F * performance;
        this.critDamageOperation = damage -> damage * critMultiplier;
        this.getEntityData().set(DATA_MAXIMUM_CRIT_MULTIPLIER_ID, totalChecks > 0 && successes >= totalChecks);
    }

    public boolean hasMaximumCritMultiplier() {
        return this.getEntityData().get(DATA_MAXIMUM_CRIT_MULTIPLIER_ID);
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    @Override
    public byte getPierceLevel() {
        if (this.piercedCount >= MAX_PIERCE_COUNT) {
            return 0;
        }
        return (byte) Math.max(MAX_PIERCE_COUNT - this.piercedCount, 1);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);

        if (!this.level().isClientSide()) {
            this.piercedCount++;

            if (this.isCritArrow() && this.hasMaximumCritMultiplier() && this.level() instanceof ServerLevel serverLevel) {
                Vec3 pos = hitResult.getEntity().position();
                serverLevel.sendParticles(
                        ParticleTypes.ENCHANTED_HIT,
                        pos.x, pos.y + hitResult.getEntity().getBbHeight() * 0.5D, pos.z,
                        50, 0.0D, 0.5D, 0.0D, 0.5D
                );
            }

            if (this.piercedCount >= MAX_PIERCE_COUNT) {
                this.destroySelf(hitResult.getLocation());
            }
        }
    }

    @Override
    protected void doKnockback(LivingEntity mob, DamageSource damageSource) {
        super.doKnockback(mob, damageSource);
        if (this.knockback > 0.0F && this.getWeaponItem() == null) {
            double knockbackResistance = Math.max(0.0D, 1.0D - mob.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 movement = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale(this.knockback * 0.6D * knockbackResistance);
            if (movement.lengthSqr() > 0.0D) {
                mob.push(movement.x, 0.1D, movement.z);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() > 1.0E-6D) {
            Vec3 dir = movement.normalize();
            double step = 0.025D;
            Vec3 hitPos = hitResult.getLocation();
            Vec3 exitPos = null;
            int steps = 0;

            for (double d = step; d <= MAX_DIG_DISTANCE; d += step) {
                Vec3 testPoint = hitPos.add(dir.scale(d));
                BlockPos testPos = BlockPos.containing(testPoint);
                BlockState state = this.level().getBlockState(testPos);
                VoxelShape shape = state.getCollisionShape(this.level(), testPos);
                steps += 1;

                boolean isCollisionInside = false;
                if (!shape.isEmpty()) {
                    for (AABB aabb : shape.toAabbs()) {
                        if (aabb.move(testPos).inflate(0.001D).contains(testPoint)) {
                            isCollisionInside = true;
                            break;
                        }
                    }
                }

                if (!isCollisionInside) {
                    exitPos = testPoint.add(dir.scale(0.05D));
                    break;
                }
            }

            if (exitPos != null) {
                // Successfully dug through block
                this.setPos(exitPos);
                this.setDeltaMovement(movement.scale(0.85D));
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                            hitPos.x, hitPos.y, hitPos.z,
                            6,
                            0.05D, 0.05D, 0.05D,
                            0.02D
                    );
                }
                this.level().playSound(
                        null,
                        hitPos.x, hitPos.y, hitPos.z,
                        SoundEvents.POINTED_DRIPSTONE_HIT,
                        SoundSource.NEUTRAL,
                        0.6F,
                        1.3F + this.random.nextFloat() * 0.3F
                );
                return;
            }

            // Cannot penetrate through: dig into the block and persist
            super.onHitBlock(hitResult);
            Vec3 digPos = hitPos.add(dir.scale(MAX_DIG_DISTANCE));
            this.setPos(digPos);
            this.setDeltaMovement(Vec3.ZERO);
            this.setInGround(true);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                        digPos.x, digPos.y, digPos.z,
                        20,
                        0.1D, 0.1D, 0.1D,
                        0.05D
                );
            }
            this.level().playSound(
                    null,
                    digPos.x, digPos.y, digPos.z,
                    SoundEvents.POINTED_DRIPSTONE_HIT,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.2F / (this.random.nextFloat() * 0.2F + 0.9F)
            );
            return;
        }

        super.onHitBlock(hitResult);
    }

    public void destroySelf(Vec3 pos) {
        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                        pos.x, pos.y, pos.z,
                        25,
                        0.15D, 0.15D, 0.15D,
                        0.05D
                );
            }
            this.level().playSound(
                    null,
                    pos.x, pos.y, pos.z,
                    SoundEvents.POINTED_DRIPSTONE_BREAK,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.2F / (this.random.nextFloat() * 0.2F + 0.9F)
            );
        }
        this.discard();
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_MAXIMUM_CRIT_MULTIPLIER_ID, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("PiercedCount", (byte) this.piercedCount);
        output.putBoolean("MaximumCritMultiplier", this.hasMaximumCritMultiplier());
        output.putDouble("CritChance", this.critChance);
        output.putFloat("Knockback", this.knockback);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.piercedCount = input.getByteOr("PiercedCount", (byte) 0);
        this.getEntityData().set(DATA_MAXIMUM_CRIT_MULTIPLIER_ID, input.getBooleanOr("MaximumCritMultiplier", false));
        this.critChance = input.getDoubleOr("CritChance", 0.05D);
        this.knockback = input.getFloatOr("Knockback", 0.4F);
    }
}
