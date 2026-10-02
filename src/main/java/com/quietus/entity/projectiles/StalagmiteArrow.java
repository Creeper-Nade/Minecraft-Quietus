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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StalagmiteArrow extends AbstractArrow implements IQuietusProjectile {

    public static final int MAX_PIERCE_COUNT = 4; // maximum entities it can pierce through
    public static final double GRAVITY = 0.05d;
    private static final double BLOCK_DIG_MINIMUM_SPEED = 0.7d; // minimum speed needed to dig through a block. If speed goes below, the arrow stops in the block
    private static final double DIG_SPEED_MULTIPLIER = 0.05d; // multiplier to its speed digging through a whole block (through 1 unit length)
    private static final double DIG_CALC_STEP_LENGTH = 0.05d; // distance of each step during block collision calculation
    private static final double DIG_SPEED_MULTIPLIER_PER_STEP = Math.pow(DIG_SPEED_MULTIPLIER, DIG_CALC_STEP_LENGTH);

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
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.lengthSqr() > 1.0E-6d) {
            BlockState penetratingBlock = this.level().getBlockState(hitResult.getBlockPos());

            Vec3 dir = velocity.normalize();
            Vec3 hitPos = hitResult.getLocation();
            Vec3 endPos = hitPos;
            Vec3 exitPos = null;

            Vec3 newVelocity = velocity;
            for (double d = DIG_CALC_STEP_LENGTH; d <= 1.0; d += DIG_CALC_STEP_LENGTH) {
                Vec3 testPoint = hitPos.add(dir.scale(d));
                BlockPos testPos = BlockPos.containing(testPoint);
                BlockState state = this.level().getBlockState(testPos);
                VoxelShape shape = state.getCollisionShape(this.level(), testPos);

                boolean isInside = false;
                if (!shape.isEmpty()) {
                    for (AABB aabb : shape.toAabbs()) {
                        if (aabb.move(testPos).inflate(0.001D).contains(testPoint)) {
                            isInside = true;
                            break;
                        }
                    }
                }

                if (!isInside) {
                    exitPos = testPoint.add(dir.scale(0.05D));
                    break;
                }
                
                newVelocity = newVelocity.scale(DIG_SPEED_MULTIPLIER_PER_STEP);
                endPos = testPoint;
                
                if (newVelocity.length() < BLOCK_DIG_MINIMUM_SPEED) {
                    break;
                }
            }

            if (exitPos != null) { // Successfully dug through block
                this.setPos(exitPos);
                this.setDeltaMovement(newVelocity);

                SoundType soundType = penetratingBlock.getSoundType(this.level(), hitResult.getBlockPos(), this);
                this.level().playSound(
                        null,
                        hitPos.x, hitPos.y, hitPos.z,
                        soundType.getHitSound(),
                        SoundSource.BLOCKS,
                        (soundType.getVolume() + 1.0F) / 2.0F,
                        soundType.getPitch() * 0.8F
                );

                this.level().playSound(
                        null,
                        hitPos.x, hitPos.y, hitPos.z,
                        SoundEvents.POINTED_DRIPSTONE_HIT,
                        SoundSource.NEUTRAL,
                        0.6F,
                        1.3F + this.random.nextFloat() * 0.3F
                );

                if (!this.level().isClientSide()) {
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                new BlockParticleOption(ParticleTypes.BLOCK, penetratingBlock),
                                hitPos.x, hitPos.y, hitPos.z,
                                6,
                                0.05D, 0.05D, 0.05D,
                                0.02D
                        );
                    }

                    if (isGlassOrGlassPane(penetratingBlock)) {
                        this.level().destroyBlock(hitResult.getBlockPos(), false, this);
                    }
                }
                return;
            }

            // Cannot penetrate through: dig into the block and persist
            super.onHitBlock(hitResult);
            //Vec3 digPos = hitPos.add(dir.scale(steps * DIG_CALC_STEP_LENGTH));
            this.setPos(endPos);
            this.setDeltaMovement(Vec3.ZERO);
            this.setInGround(true);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                        endPos.x, endPos.y, endPos.z,
                        20,
                        0.1D, 0.1D, 0.1D,
                        0.05D
                );
            }
            this.level().playSound(
                    null,
                    endPos.x, endPos.y, endPos.z,
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

    private static boolean isGlassOrGlassPane(BlockState state) {
        return state.is(Tags.Blocks.GLASS_BLOCKS)
                || state.is(Tags.Blocks.GLASS_PANES)
                || state.is(Blocks.GLASS)
                || state.is(Blocks.GLASS_PANE)
                || state.is(Blocks.TINTED_GLASS)
                || state.getBlock() instanceof StainedGlassPaneBlock;
    }
}
