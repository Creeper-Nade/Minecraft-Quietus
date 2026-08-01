package com.quietus.entity.monster;

import com.quietus.mixin.SkeletonConversionAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** A skeleton whose combat role is independent from its biome appearance. */
public abstract class ThemedSkeleton extends Skeleton implements Shearable {
    private static final String APPEARANCE_TAG = "SkeletonAppearance";
    private static final String SHEARED_TAG = "ThemedBoggedSheared";
    private static final EntityDataAccessor<Integer> DATA_APPEARANCE =
            SynchedEntityData.defineId(ThemedSkeleton.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SHEARED =
            SynchedEntityData.defineId(ThemedSkeleton.class, EntityDataSerializers.BOOLEAN);

    protected ThemedSkeleton(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_APPEARANCE, SkeletonAppearance.SKELETON.ordinal());
        entityData.define(DATA_SHEARED, false);
    }

    public SkeletonAppearance getSkeletonAppearance() {
        return SkeletonAppearance.byId(this.entityData.get(DATA_APPEARANCE));
    }

    public void setSkeletonAppearance(SkeletonAppearance appearance) {
        this.entityData.set(DATA_APPEARANCE, appearance.ordinal());
        if (appearance != SkeletonAppearance.BOGGED) {
            this.setSheared(false);
        }
        if (appearance == SkeletonAppearance.STRAY) {
            this.setFreezeConverting(false);
            ((SkeletonConversionAccessor) this).quietus$setInPowderSnowTime(Integer.MIN_VALUE);
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        this.setSkeletonAppearance(SkeletonAppearance.fromBiome(level.getBiome(this.blockPosition())));
        return result;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(APPEARANCE_TAG, this.getSkeletonAppearance().ordinal());
        output.putBoolean(SHEARED_TAG, this.isSheared());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSkeletonAppearance(SkeletonAppearance.byId(input.getIntOr(APPEARANCE_TAG, 0)));
        this.setSheared(input.getBooleanOr(SHEARED_TAG, false));
    }

    /** Entity-type properties that vary per appearance cannot be represented by one static registry tag. */
    @Override
    public boolean is(TagKey<EntityType<?>> tag) {
        if (tag == EntityTypeTags.BURN_IN_DAYLIGHT) {
            return this.getSkeletonAppearance() != SkeletonAppearance.PARCHED;
        }
        if (tag == EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES) {
            return this.getSkeletonAppearance() == SkeletonAppearance.STRAY || super.is(tag);
        }
        return super.is(tag);
    }

    /** Applies the projectile trait belonging to the active vanilla skeleton theme. */
    public AbstractArrow applySkeletonTheme(AbstractArrow arrow) {
        if (arrow instanceof Arrow vanillaArrow) {
            switch (this.getSkeletonAppearance()) {
                case STRAY -> vanillaArrow.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 600));
                case BOGGED -> vanillaArrow.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
                case PARCHED -> vanillaArrow.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600));
                default -> {
                }
            }
        }
        return arrow;
    }

    @Override
    protected AbstractArrow getArrow(ItemStack projectile, float power, @Nullable ItemStack firingWeapon) {
        return this.applySkeletonTheme(super.getArrow(projectile, power, firingWeapon));
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return this.getSkeletonAppearance() != SkeletonAppearance.PARCHED
                || effect.getEffect() != MobEffects.WEAKNESS
                ? super.canBeAffected(effect)
                : false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (this.getSkeletonAppearance()) {
            case STRAY -> SoundEvents.STRAY_AMBIENT;
            case BOGGED -> SoundEvents.BOGGED_AMBIENT;
            case PARCHED -> SoundEvents.PARCHED_AMBIENT;
            default -> SoundEvents.SKELETON_AMBIENT;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return switch (this.getSkeletonAppearance()) {
            case STRAY -> SoundEvents.STRAY_HURT;
            case BOGGED -> SoundEvents.BOGGED_HURT;
            case PARCHED -> SoundEvents.PARCHED_HURT;
            default -> SoundEvents.SKELETON_HURT;
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return switch (this.getSkeletonAppearance()) {
            case STRAY -> SoundEvents.STRAY_DEATH;
            case BOGGED -> SoundEvents.BOGGED_DEATH;
            case PARCHED -> SoundEvents.PARCHED_DEATH;
            default -> SoundEvents.SKELETON_DEATH;
        };
    }

    @Override
    protected SoundEvent getStepSound() {
        return switch (this.getSkeletonAppearance()) {
            case STRAY -> SoundEvents.STRAY_STEP;
            case BOGGED -> SoundEvents.BOGGED_STEP;
            case PARCHED -> SoundEvents.PARCHED_STEP;
            default -> SoundEvents.SKELETON_STEP;
        };
    }

    public boolean isSheared() {
        return this.entityData.get(DATA_SHEARED);
    }

    public void setSheared(boolean sheared) {
        this.entityData.set(DATA_SHEARED, sheared);
    }

    @Override
    public void shear(ServerLevel level, SoundSource soundSource, ItemStack tool) {
        if (!this.readyForShearing()) {
            return;
        }
        level.playSound(null, this, SoundEvents.BOGGED_SHEAR, soundSource, 1.0F, 1.0F);
        this.dropFromShearingLootTable(
                level,
                BuiltInLootTables.BOGGED_SHEAR,
                tool,
                (ignored, drop) -> this.spawnAtLocation(level, drop, this.getBbHeight()));
        this.setSheared(true);
    }

    @Override
    public boolean readyForShearing() {
        return this.getSkeletonAppearance() == SkeletonAppearance.BOGGED
                && !this.isSheared()
                && this.isAlive();
    }

    /** Keeps this entity and its role AI, changing only its synced/persisted appearance. */
    @Override
    protected void doFreezeConversion() {
        this.setSkeletonAppearance(SkeletonAppearance.STRAY);
        if (!this.isSilent()) {
            this.level().levelEvent(null, 1048, this.blockPosition(), 0);
        }
    }
}
