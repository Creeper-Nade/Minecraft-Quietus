package com.minecraftquietus.quietus.entity.monster;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.minecraftquietus.quietus.entity.ai.goal.ParabolaAttackGoal;
import com.minecraftquietus.quietus.item.weapons.AmmoProjectileWeaponItem;
import com.minecraftquietus.quietus.item.weapons.QuietusProjectileWeaponItem;

public class Paraboler extends Skeleton {

    private final RangedBowAttackGoal<AbstractSkeleton> bowGoal;
    private final ParabolaAttackGoal<AbstractSkeleton> parabolaBowGoal;
    private final MeleeAttackGoal meleeGoal;

    private byte selectedAttackGoal = 2; // 2 for meleeGoa, 1 for bowGoal, 0 for parabolaBowGoal

    private static final int HARD_ATTACK_INTERVAL_BASE = 12;
    private static final int NORMAL_ATTACK_INTERVAL_BASE = 15;
    private static final double ABNORMAL_INTERVAL_MULTIPLIER = 2.5;
    private int hardAttackInterval = 12;
    private int normalAttackInterval = 15;


    /**
     * Methods for parabola:
     * Given relative position of target to this and find initial velocities:
     * V0x = TargetX÷time
     * V0y = TargetY÷time + 1/2*gravity*time
     * V0z = TargetZ÷time
     */
    public Paraboler(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
        this.bowGoal = new RangedBowAttackGoal<>(this, 1.25, 40, 20.0F);
        this.parabolaBowGoal = new ParabolaAttackGoal<>(this, 1.00, 4, 17.0F);
        this.meleeGoal = new MeleeAttackGoal(this, 1.2, false) {
                @Override
                public void stop() {
                    super.stop();
                    Paraboler.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    Paraboler.this.setAggressive(true);
                }
            };
        this.reassessWeaponGoal();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new RestrictSunGoal(this));
        this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Wolf.class, 6.0F, 1.0, 1.2));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder dataBuild) {
        super.defineSynchedData(dataBuild);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
    } 

    
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData
    ) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        RandomSource random = level.getRandom();
        this.populateDefaultEquipmentSlots(random, difficulty);
        this.populateDefaultEquipmentEnchantments(level, random, difficulty);
        this.reassessWeaponGoal();
        this.setCanPickUpLoot(random.nextFloat() < 0.55F * difficulty.getSpecialMultiplier());
        /* halloween pumpkin head
        if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate localdate = LocalDate.now();
            int i = localdate.get(ChronoField.DAY_OF_MONTH);
            int j = localdate.get(ChronoField.MONTH_OF_YEAR);
            if (j == 10 && i == 31 && randomsource.nextFloat() < 0.25F) {
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(randomsource.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.setDropChance(EquipmentSlot.HEAD, 0.0F);
            }
        }*/

        return spawnGroupData;
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 20.0);
    }

    /**
     * Attack the specified entity using a ranged attack.
     */
    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        ItemStack weapon = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof net.minecraft.world.item.ProjectileWeaponItem));
        ItemStack ammo = this.getProjectile(weapon);
        ProjectileWeaponItem weapon_item = (ProjectileWeaponItem)weapon.getItem();
        if (this.level() instanceof ServerLevel level) {
            if (weapon_item instanceof QuietusProjectileWeaponItem quietus_weapon_item && quietus_weapon_item.getUseDuration(weapon, this) > 0 && quietus_weapon_item.getPowerDuration(weapon, this) >= 0) { // Quietus' AmmoProjectileWeaponItem and is bow
                List<ItemStack> _projectile_item = new ArrayList<>(quietus_weapon_item.getProjectilesPerShot());
                for (int i = 0; i < quietus_weapon_item.getProjectilesPerShot(); i ++) _projectile_item.add(ammo); 
                quietus_weapon_item.shoot(level,this,ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof net.minecraft.world.item.ProjectileWeaponItem),weapon,_projectile_item,velocity,1.6f,false,null);
            } else if (weapon_item instanceof BowItem bow) { // vanilla BowItem
                AbstractArrow abstractarrow = this.getArrow(ammo, velocity, weapon);
                abstractarrow = bow.customArrow(abstractarrow, ammo, weapon);
                double d0 = target.getX() - this.getX();
                double d1 = target.getY(0.3333333333333333) - abstractarrow.getY();
                double d2 = target.getZ() - this.getZ();
                double d3 = Math.sqrt(d0 * d0 + d2 * d2);
                Projectile.spawnProjectileUsingShoot(
                    abstractarrow, level, ammo, d0, d1 + d3 * 0.19f, d2, 1.6F, 12
                );
            }
        } 
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    @Override
    public void reassessWeaponGoal() {
        if (bowGoal == null || parabolaBowGoal == null || meleeGoal == null) {
            return; // stop if goals are not yet initialzed (expected to stop the call on this method by constructor by super AbstractSkeleton)
        }
        if (this.level() != null && !this.level().isClientSide) {
            // removal of actual goals
            this.goalSelector.removeGoal(this.parabolaBowGoal);
            this.goalSelector.removeGoal(this.bowGoal);
            this.goalSelector.removeGoal(this.meleeGoal);
            
            ItemStack itemstackBow = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof net.minecraft.world.item.ProjectileWeaponItem));
            boolean hasBow = 
                itemstackBow.getItem() instanceof BowItem
                || (itemstackBow.getItem() instanceof QuietusProjectileWeaponItem it && it.getUseDuration(itemstackBow, this) > 0 && it.getPowerDuration(itemstackBow, this) >= 0);
            if (hasBow) {
                boolean hasMultiProjectileBow = 
                    itemstackBow.getItem() instanceof QuietusProjectileWeaponItem it && it.getProjectilesPerShot() > 1;
                this.setAttackInterval(hasMultiProjectileBow);
                int i = this.getHardAttackInterval();
                if (this.level().getDifficulty() != Difficulty.HARD) {
                    i = this.getAttackInterval();
                }
                if (hasMultiProjectileBow) {
                    this.parabolaBowGoal.setMinAttackInterval(i);
                    this.goalSelector.addGoal(4, this.parabolaBowGoal);
                    this.selectedAttackGoal = 0;
                } else {
                    this.bowGoal.setMinAttackInterval(i);
                    this.goalSelector.addGoal(4, this.bowGoal);
                    this.selectedAttackGoal = 1;
                }
            } else { // melee attack; same as normal skeleton
                this.setAttackInterval(true);
                this.goalSelector.addGoal(4, this.meleeGoal);
                this.selectedAttackGoal = 2;
            }
        }
    }
    
    @Override
    protected int getHardAttackInterval() {
        return this.hardAttackInterval;
    }
    @Override
    protected int getAttackInterval() {
        return this.normalAttackInterval;
    }
    protected void setAttackInterval(boolean normalAttack) {
        double multiplier = normalAttack ? 1.0d : ABNORMAL_INTERVAL_MULTIPLIER;
        this.hardAttackInterval = (int)((double)HARD_ATTACK_INTERVAL_BASE * multiplier);
        this.normalAttackInterval = (int)((double)NORMAL_ATTACK_INTERVAL_BASE * multiplier);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }
}
