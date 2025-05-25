package com.minecraftquietus.quietus.entity.monster;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

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
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.minecraftquietus.quietus.entity.ai.goal.VolleyAttackGoal;

public class Bowslinger extends Skeleton implements VolleyRangedAttackMob {

    private RangedBowAttackGoal<AbstractSkeleton> bowGoal;
    private VolleyAttackGoal<Bowslinger> gunslingerBowGoal;
    private MeleeAttackGoal meleeGoal;
    private static final EntityDataAccessor<Integer> DATA_BOWSLINGER_VOLLEY = SynchedEntityData.defineId(Bowslinger.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BOWSLINGER_VOLLEY_MAX = SynchedEntityData.defineId(Bowslinger.class, EntityDataSerializers.INT);
    public static final String VOLLEY_TAG = "BowslingerVolley";
    public static final String VOLLEY_MAX_TAG = "BowslingerVolleyMax";

    private static final int HARD_ATTACK_INTERVAL_BASE = 42;
    private static final int NORMAL_ATTACK_INTERVAL_BASE = 50;
    private static final double ABNORMAL_INTERVAL_MULTIPLIER = 0.15;
    private int hardAttackInterval = 3;
    private int normalAttackInterval = 4;

    public Bowslinger(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
        this.reassessWeaponGoal();
    }

    public int getVolley() {
        return this.getEntityData().get(DATA_BOWSLINGER_VOLLEY);
    }
    public int getVolleyMax() {
        return this.getEntityData().get(DATA_BOWSLINGER_VOLLEY_MAX);
    }
    public void setVolley(int value) {
        this.entityData.set(DATA_BOWSLINGER_VOLLEY, value);
    }
    public void setVolleyMax(int value) {
        this.entityData.set(DATA_BOWSLINGER_VOLLEY_MAX, value);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder dataBuild) {
        super.defineSynchedData(dataBuild);
        dataBuild.define(DATA_BOWSLINGER_VOLLEY, 0);
        dataBuild.define(DATA_BOWSLINGER_VOLLEY_MAX, 6+this.random.nextInt(2));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(VOLLEY_TAG, this.getVolley());
        tag.putInt(VOLLEY_MAX_TAG, this.getVolleyMax());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        int i = tag.getIntOr(VOLLEY_TAG, 0);
        int j = tag.getIntOr(VOLLEY_MAX_TAG, 6+this.random.nextInt(2));
        this.setVolley(i);
        this.setVolleyMax(j);
    }

    
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.BOW));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData
    ) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        RandomSource randomsource = level.getRandom();
        this.populateDefaultEquipmentSlots(randomsource, difficulty);
        this.populateDefaultEquipmentEnchantments(level, randomsource, difficulty);
        this.reassessWeaponGoal();
        this.setCanPickUpLoot(randomsource.nextFloat() < 0.55F * difficulty.getSpecialMultiplier());
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

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 18.0);
    }

    /**
     * Attack the specified entity using a ranged attack.
     */
    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        ItemStack weapon = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof net.minecraft.world.item.BowItem));
        ItemStack itemstack1 = this.getProjectile(weapon);
        AbstractArrow abstractarrow = this.getArrow(itemstack1, velocity, weapon);
        if (weapon.getItem() instanceof net.minecraft.world.item.ProjectileWeaponItem weaponItem)
            abstractarrow = weaponItem.customArrow(abstractarrow, itemstack1, weapon);
        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333) - abstractarrow.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        if (this.level() instanceof ServerLevel serverlevel) {
            Projectile.spawnProjectileUsingShoot(
                abstractarrow, serverlevel, itemstack1, d0, d1 + d3 * 0.19f, d2, 1.6F, 15
            );
        }

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    @Override
    public void reassessWeaponGoal() {
        if (this.level() != null && !this.level().isClientSide) {
            // D for dynamic objects; made dynamic to avoid goals not initialized before putting into goal selector.
            RangedBowAttackGoal<AbstractSkeleton> bowGoalD = new RangedBowAttackGoal<>(this, 1.25, 40, 20.0F);
            VolleyAttackGoal<Bowslinger> gunslingerBowGoalD = new VolleyAttackGoal<>(this, 1.25, 4, 17.0F);
            MeleeAttackGoal meleeGoalD = new MeleeAttackGoal(this, 1.2, false) {
                @Override
                public void stop() {
                    super.stop();
                    Bowslinger.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    Bowslinger.this.setAggressive(true);
                }
            };
            // removal of actual goals
            this.goalSelector.removeGoal(this.gunslingerBowGoal);
            this.goalSelector.removeGoal(this.bowGoal);
            this.goalSelector.removeGoal(this.meleeGoal);
            
            ItemStack itemstackBow = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof net.minecraft.world.item.BowItem));
            ItemStack itemstackMain = this.getMainHandItem();
            ItemStack itemstackOff = this.getOffhandItem();
            if (itemstackBow.getItem() instanceof net.minecraft.world.item.BowItem) {
                boolean hasDuelBow = itemstackMain.getItem() instanceof net.minecraft.world.item.BowItem && itemstackOff.getItem() instanceof net.minecraft.world.item.BowItem;
                this.setAttackInterval(hasDuelBow);
                int i = this.getHardAttackInterval();
                if (this.level().getDifficulty() != Difficulty.HARD) {
                    i = this.getAttackInterval();
                }
                if (hasDuelBow) {
                    gunslingerBowGoalD.setMinAttackInterval(i);
                    this.gunslingerBowGoal = gunslingerBowGoalD;
                    this.goalSelector.addGoal(4, this.gunslingerBowGoal);
                } else {
                    bowGoalD.setMinAttackInterval(i);
                    this.bowGoal = bowGoalD;
                    this.goalSelector.addGoal(4, this.bowGoal);
                }
            } else { // melee attack; same as normal skeleton
                this.setAttackInterval(true);
                this.goalSelector.addGoal(4, meleeGoalD);
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
