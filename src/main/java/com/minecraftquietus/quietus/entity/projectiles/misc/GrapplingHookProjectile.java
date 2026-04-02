package com.minecraftquietus.quietus.entity.projectiles.misc;

import com.minecraftquietus.quietus.core.GrapplingHookAttachment;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectile;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectiles;
import com.minecraftquietus.quietus.item.QuietusItems;
import com.minecraftquietus.quietus.item.property.GrapplingHookProperty;
import com.minecraftquietus.quietus.item.tool.GrapplingHookItem;
import com.minecraftquietus.quietus.tags.QuietusTags;
import com.minecraftquietus.quietus.util.PlayerData;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class GrapplingHookProjectile extends QuietusProjectile {
    private static final EntityDataAccessor<Boolean> IN_BLOCK =
            SynchedEntityData.defineId(GrapplingHookProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> LENGTH =
            SynchedEntityData.defineId(GrapplingHookProjectile.class, EntityDataSerializers.FLOAT);

    // Synced properties from GrapplingHookProperty
    private static final EntityDataAccessor<Float> DATA_MAX_TRAVEL_DISTANCE =
            SynchedEntityData.defineId(GrapplingHookProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PULL_STRENGTH =
            SynchedEntityData.defineId(GrapplingHookProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_FRICTION_MULTIPLIER =
            SynchedEntityData.defineId(GrapplingHookProjectile.class, EntityDataSerializers.FLOAT);


    private GrapplingHookProperty grapplingHookProperty;

    public GrapplingHookProjectile(EntityType<? extends GrapplingHookProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IN_BLOCK, false);
        builder.define(LENGTH, 0.0F);
        builder.define(DATA_MAX_TRAVEL_DISTANCE, 100.0F);
        builder.define(DATA_PULL_STRENGTH, 0.1F);
        builder.define(DATA_FRICTION_MULTIPLIER, 0.99F);
    }

    @Override
    protected void spawnImpactParticles() {

    }

    @Override
    protected void spawnTrailParticles() {

    }
    @Override
    public void tick() {

        // If we're stuck in a block, don't move
        if (this.isInBlock()) {
            // Maintain position and don't apply gravity
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);

            // Check for discard
            if (this.tickCount > this.persistanceTicks && !level().isClientSide) {
                discardAction();
            }
            return;
        }

        // Use arrow-like collision detection
        boolean flag = true; // Always enable physics for grappling hook
        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level().getBlockState(blockpos);

        // Check for voxel shape collision like arrows do
        if (!blockstate.isAir() && flag) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
            if (!voxelshape.isEmpty()) {
                Vec3 vec31 = this.position();

                for (AABB aabb : voxelshape.toAabbs()) {
                    if (aabb.move(blockpos).contains(vec31)) {
                        this.setDeltaMovement(Vec3.ZERO);
                        this.setInBlock(true);
                        this.setNoGravity(true);

                        if (this.getOwner() != null && grapplingHookProperty != null) {
                            // Calculate rope length based on distance
                            double distance = this.getOwner().getEyePosition().distanceTo(vec31);
                            float length = (float) Math.min(distance, grapplingHookProperty.maxRange());
                            this.setLength(length);
                        }
                        return;
                    }
                }
            }
        }

        // Only apply gravity if not in block
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -this.gravity, 0.0D));
        }

        // --- Tethering: smooth restoring force when beyond maxTravelDistance ---
        if (this.getOwner() instanceof Player player) {
            // Ensure same dimension
            if (player.level().dimension() == this.level().dimension()) {
                double maxDist = getMaxTravelDistance();
                Vec3 toPlayer = this.position().subtract(player.position());
                double distSq = toPlayer.lengthSqr();
                if (distSq > maxDist * maxDist) {

                    Vec3 dir = toPlayer.normalize();
                    double dist = Math.sqrt(distSq);
                    double overshoot = dist - maxDist;

                    // Spring constant – higher values pull back more aggressively
                    double springStrength = 0.2;
                    double targetRadialSpeed = -springStrength * overshoot; // negative = toward player

                    // Current radial speed (positive = moving away)
                    double radialSpeed = this.getDeltaMovement().dot(dir);
                    // Smoothly adjust radial speed toward target
                    double smoothing = 0.3; // per‑tick interpolation factor
                    double newRadialSpeed = radialSpeed + (targetRadialSpeed - radialSpeed) * smoothing;

                    // Keep tangential component and apply damping
                    Vec3 tangential = this.getDeltaMovement().subtract(dir.scale(radialSpeed));
                    tangential = tangential.scale(0.98); // air resistance on swing



                    // Re‑combine velocities
                    this.setDeltaMovement(tangential.add(dir.scale(newRadialSpeed)));

                    // Global speed cap (optional, prevents extreme speeds)
                    double speed = this.getDeltaMovement().length();
                    double maxSpeed = 3.0; // adjust per hook type if needed
                    if (speed > maxSpeed) {
                        this.setDeltaMovement(this.getDeltaMovement().scale(maxSpeed / speed));
                    }
                }
            }
        }

        // Calculate movement
        Vec3 vec32 = this.position();
        Vec3 vec33 = vec32.add(this.getDeltaMovement());



        // Perform raycast for collision
        HitResult hitresult = this.level().clip(new ClipContext(
                vec32,
                vec33,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
        ));

        if (hitresult.getType() != HitResult.Type.MISS) {
            vec33 = hitresult.getLocation();
        }

        // Set new position
        this.setPos(vec33);

        // Update rotation
        Vec3 vec34 = this.getDeltaMovement();
        float f = (float)(Math.atan2(vec34.x, vec34.z) * 180.0D / Math.PI);
        this.setYRot(f);
        f = (float)(Math.atan2(vec34.y, Math.sqrt(vec34.x * vec34.x + vec34.z * vec34.z)) * 180.0D / Math.PI);
        this.setXRot(f);

        // Apply inertia/friction
        if (this.isInWater()) {
            this.setDeltaMovement(vec34.scale(0.8D));
        } else {
            this.setDeltaMovement(vec34.scale(0.99D)); // Air friction
        }




        // Check for timeout
        if (this.tickCount > this.persistanceTicks && !level().isClientSide) {
            discardAction();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        //System.out.println("1");
        //discardAction();
/*
        if (!this.isInBlock()) {
            this.setInBlock(true);

            if (this.getOwner() != null && grapplingHookProperty != null) {
                // Calculate rope length based on distance
                double distance = this.getOwner().getEyePosition().distanceTo(hitResult.getLocation());
                float length = (float) Math.min(distance, grapplingHookProperty.maxRange());
                this.setLength(length);
            }
        }
        this.setDeltaMovement(Vec3.ZERO);*/

        //this.setPos(hitResult.getLocation());
        //this.setDeltaMovement(this.getDeltaMovement().normalize().scale(hitResult.distanceTo(this)));
    }
    @Nullable
    public Player getPlayerOwner() {
        Entity var2 = this.getOwner();
        Player var10000;
        if (var2 instanceof Player player) {
            var10000 = player;
        } else {
            var10000 = null;
        }

        return var10000;
    }
    @Override
    protected void onHitEntity(EntityHitResult hitResult)
    {
        //might implement grappling of entity in the future
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        // Don't hit entities, only blocks
        return false;
    }

    @Override
    protected void applyImpactEffects(Entity Target, float damage, boolean is_crit, Entity Owner) {

    }

    @Override
    protected DamageSource getDamageSource(Entity owner) {
        return null;
    }

    public void setInBlock(boolean inBlock) {
        this.entityData.set(IN_BLOCK, inBlock);
    }

    public boolean isInBlock() {
        return this.entityData.get(IN_BLOCK);
    }

    public void setLength(float length) {
        this.entityData.set(LENGTH, length);
    }

    public float getLength() {
        return this.entityData.get(LENGTH);
    }
    public float getMaxRange()
    {
        return this.grapplingHookProperty.maxRange();
    }

    public void setGrapplingHookProperty(GrapplingHookProperty property) {
        this.grapplingHookProperty = property;
        // Sync values to client
        this.entityData.set(DATA_MAX_TRAVEL_DISTANCE, property.maxTravelDistance());
        this.entityData.set(DATA_PULL_STRENGTH, property.pullStrength()); // used as stiffness
        this.entityData.set(DATA_FRICTION_MULTIPLIER, property.frictionMultiplier());
    }

    public float getPullStrength() {
        return this.entityData.get(DATA_PULL_STRENGTH);
    }

    public float getFrictionMultiplier() {
        return this.entityData.get(DATA_FRICTION_MULTIPLIER);
    }
    public float getMaxPullSpeed() {
        return grapplingHookProperty != null ? grapplingHookProperty.maxPullSpeed() : 2.0F;
    }
    public float getMaxTravelDistance() {
        return this.entityData.get(DATA_MAX_TRAVEL_DISTANCE);
    }
    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (this.getOwner() instanceof Player player) {
            GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);
            if (attachment.getHookEntityId() == this.getId()) {
                attachment.clear();
                PlayerData.sendGrappleActivityPackToEntity(player,attachment.hasActiveHook());
            }
        }
    }


    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("in_block", this.isInBlock());
        tag.putFloat("length", this.getLength());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setInBlock(tag.getBooleanOr("in_block",false));
        this.setLength(tag.getFloatOr("length",0));
    }

}
