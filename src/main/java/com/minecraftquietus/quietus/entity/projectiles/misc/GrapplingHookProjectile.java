package com.minecraftquietus.quietus.entity.projectiles.misc;

import com.minecraftquietus.quietus.core.GrapplingHookAttachment;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectile;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectiles;
import com.minecraftquietus.quietus.item.QuietusItems;
import com.minecraftquietus.quietus.item.property.GrapplingHookProperty;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GrapplingHookProjectile extends Projectile {
    private static final EntityDataAccessor<Boolean> IN_BLOCK =
            SynchedEntityData.defineId(GrapplingHookProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> LENGTH =
            SynchedEntityData.defineId(GrapplingHookProjectile.class, EntityDataSerializers.FLOAT);

    private GrapplingHookProperty grapplingHookProperty;
    private int timeInAir = 0;

    public GrapplingHookProjectile(EntityType<? extends GrapplingHookProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // Enable gravity
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(IN_BLOCK, false);
        builder.define(LENGTH, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();

        Player player = this.getPlayerOwner();
        if (player == null) {
            this.discard();
            return;
        }

        if (!this.isInBlock()) {
            timeInAir++;

            // Apply gravity
            applyGravity();

            // Move the projectile
            Vec3 motion = this.getDeltaMovement();
            double nextX = this.getX() + motion.x;
            double nextY = this.getY() + motion.y;
            double nextZ = this.getZ() + motion.z;

            // Check for collisions
            this.setPos(nextX, nextY, nextZ);
            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

            if (hitResult.getType() != HitResult.Type.MISS) {
                this.onHit(hitResult);
            }

            // Check max range
            if (grapplingHookProperty != null) {
                double distanceSqr = this.distanceToSqr(player);
                if (distanceSqr > grapplingHookProperty.maxRange() * grapplingHookProperty.maxRange()) {
                    this.discard();
                    return;
                }
            }

            // Update rotation
            this.updateRotation();
        }

        // Remove if owner is gone
        if (player.isRemoved() || !player.isAlive()) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);

        if (!this.isInBlock()) {
            // Stick to the block
            this.setDeltaMovement(Vec3.ZERO);
            this.setInBlock(true);

            Player player = this.getPlayerOwner();
            if (player != null && grapplingHookProperty != null) {
                // Calculate rope length based on distance
                double distance = player.getEyePosition().distanceTo(hitResult.getLocation());
                float length = (float) Math.min(distance, grapplingHookProperty.maxRange());
                this.setLength(length);
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        // Don't hit entities, only blocks
        return false;
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

    public void setGrapplingHookProperty(GrapplingHookProperty property) {
        this.grapplingHookProperty = property;
    }

    public Player getPlayerOwner() {
        Entity owner = this.getOwner();
        return owner instanceof Player ? (Player) owner : null;
    }

    public float getPullStrength() {
        return grapplingHookProperty != null ? grapplingHookProperty.pullStrength() : 0.1F;
    }

    public float getFrictionMultiplier() {
        return grapplingHookProperty != null ? grapplingHookProperty.frictionMultiplier() : 0.99F;
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

    public void updateRotation() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() != 0.0D) {
            this.setYRot((float)(Math.atan2(motion.x, motion.z) * (180.0D / Math.PI)));
            this.setXRot((float)(Math.atan2(motion.y, Math.sqrt(motion.x * motion.x + motion.z * motion.z)) * (180.0D / Math.PI)));
        }
    }
}
