package com.minecraftquietus.quietus.entity.monster;

import com.minecraftquietus.quietus.client.particle.particle_options.DustExplosionParticleOptions;
import com.minecraftquietus.quietus.client.particle.particle_options.DustImplosionParticleOptions;
import com.minecraftquietus.quietus.entity.QuietusEntityDataSerializers;
import com.minecraftquietus.quietus.entity.ai.goal.NoWallCheckAttackableGoal;
import com.minecraftquietus.quietus.sounds.QuietusSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animatable.processing.AnimationController;
import com.geckolib.animatable.processing.AnimationTest;
import com.geckolib.animation.PlayState;
import com.geckolib.animation.RawAnimation;
import com.geckolib.constant.DataTickets;
import com.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class PlayerFragment extends PathfinderMob implements GeoEntity {
    //private static final EntityDataAccessor<ItemStack> HEAD_ITEM = SynchedEntityData.defineId(PlayerGhost.class, EntityDataSerializers.ITEM_STACK);
    //private Identifier HeadTexture;
    private static final EntityDataAccessor<Identifier> HeadTexture= SynchedEntityData.defineId(PlayerFragment.class, QuietusEntityDataSerializers.RESOURCE_LOCATION.get());
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final SimpleContainer lootInventory = new SimpleContainer(27);
    private int storedExperience; // Stores the total XP points

    private static final EntityDataAccessor<Boolean> IS_SPAWNING = SynchedEntityData.defineId(PlayerFragment.class, EntityDataSerializers.BOOLEAN);
    private int spawnTime = 0;
    private static final int TOTAL_SPAWN_TIME = 40; // 3 seconds at 20 ticks/sec

    private static final EntityDataAccessor<Boolean> HAS_TARGET = SynchedEntityData.defineId(PlayerFragment.class, EntityDataSerializers.BOOLEAN);

    // Animation state tracking
    //private int noTargetTicks = 0;
    //private static final int MAX_NO_TARGET_TICKS = 100;

    //declare animations
    protected static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("animation.player_fragment.walk");
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.player_fragment.idle");
    protected static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.player_fragment.attack");
    protected static final RawAnimation SPAWN_ANIM = RawAnimation.begin().thenPlay("animation.player_fragment.spawn");

    public PlayerFragment(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this,10,true);
        this.addEffect(new MobEffectInstance(MobEffects.GLOWING,-1, 1,false,false));
        //HeadTexture = DefaultPlayerSkin.getDefaultSkin().texture();
        setPersistenceRequired();
        //this.noPhysics = true;
        //this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder dataBuild) {
        super.defineSynchedData(dataBuild);;
        //dataBuild.define(HEAD_ITEM, ItemStack.EMPTY);
        dataBuild.define(HeadTexture,DefaultPlayerSkin.getDefaultSkin().texture());
        dataBuild.define(HAS_TARGET, false);
        dataBuild.define(IS_SPAWNING, false); // Add this line
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        //save experience
        tag.putInt("StoredExperience", this.storedExperience);
        // Save the texture location to the NBT tag
        if (this.getPlayerHeadTexture() != DefaultPlayerSkin.getDefaultSkin().texture()) {
            tag.putString("HeadTexture", this.getPlayerHeadTexture().toString());
        }
        // Save the loot inventory to the NBT tag
        ContainerHelper.saveAllItems(tag, this.lootInventory.getItems(),this.level().registryAccess());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Read the texture location from the NBT tag
        if (tag.contains("HeadTexture")) {
            Identifier savedTexture = Identifier.parse(tag.getStringOr("HeadTexture","null"));
            this.entityData.set(HeadTexture, savedTexture);
        }
        this.storedExperience = tag.getIntOr("StoredExperience",0);

        ContainerHelper.loadAllItems(tag, this.lootInventory.getItems(),this.level().registryAccess());
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FLYING_SPEED, 0.8D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 25.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    public void setPlayerData(Player player) {
        //pass head texture
        Minecraft minecraft = Minecraft.getInstance();
        Identifier headTex=minecraft.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
        entityData.set(HeadTexture, headTex);

        this.setStoredExperience(calculateTotalExperience(player));
        setPlayerName(player);

        // Initialize spawn stats
        this.spawnTime = TOTAL_SPAWN_TIME;
        AttributeInstance maxHealth=  this.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attack_dmg=  this.getAttribute(Attributes.ATTACK_DAMAGE);
        double health = calculateFragmentStat(storedExperience, maxHealth.getBaseValue(),0.5f,80.0f);
        maxHealth.setBaseValue(health);
        this.setHealth((float) health);
        attack_dmg.setBaseValue(calculateFragmentStat(storedExperience,attack_dmg.getBaseValue(),0.1f,8));

        this.makeSound(this.getPreSpawnSound());
    }
    private double calculateFragmentStat(int totalExperience, double base, double factor, double max) {
        return Math.min(base + (Math.sqrt(totalExperience) * factor), max);
    }
    //total experience has to be converted from level, because the total experience defined in the player class doesn't sync with level change
    private int calculateTotalExperience(Player player) {
        // Calculate total XP from level and progress
        return getExperienceForLevel(player.experienceLevel) +
                (int)(player.experienceProgress * getXpForNextLevel(player.experienceLevel));
    }

    private int getExperienceForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int)(2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int)(4.5 * level * level - 162.5 * level + 2220);
        }
    }

    private int getXpForNextLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    public Identifier getPlayerHeadTexture()
    {
        return entityData.get(HeadTexture);
    }


    public void setLoot(List<ItemStack> loot) {
        lootInventory.clearContent();
        for (int i = 0; i < loot.size() && i < lootInventory.getContainerSize(); i++) {
            ItemStack stack = loot.get(i);
            // Only add non-empty stacks
            if (!stack.isEmpty()) {
                lootInventory.setItem(i, stack);
            }
        }
    }
    // Method to set the experience from the player
    public void setStoredExperience(int xp) {
        this.storedExperience = xp;
    }

    // Method to get the stored experience
    public int getStoredExperience() {
        return this.storedExperience;
    }

    public void setPlayerName(Player player) {
        // Set the custom name to player's name + "'s Fragment"
        this.setCustomName(Component.translatable("entity.quietus.player_fragment.suffix", player.getName()));
        this.setCustomNameVisible(true); // Make sure the name is always visible
    }

    public boolean isSpawning() {
        return this.entityData.get(IS_SPAWNING);
    }

    public float getSpawnProgress() {
        return 1.0f - ((float) this.spawnTime / TOTAL_SPAWN_TIME);
    }

    public int getSpawnTime() {
        return this.spawnTime;
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        this.setNoGravity(true);
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            // Handle spawn state
            Vec3 pos = this.position();
            if (this.spawnTime > 0) {
                this.spawnTime--;
                this.entityData.set(IS_SPAWNING, true);
                // Stop movement during spawn
                this.getNavigation().stop();
                int half_spawn_time=TOTAL_SPAWN_TIME/2;
                if(this.spawnTime>half_spawn_time)
                {
                    int particleNumber= 20*(this.spawnTime-half_spawn_time)/ half_spawn_time;
                    level.sendParticles(
                            new DustImplosionParticleOptions(new Vec3(1, 1, 1),1.5f),
                            pos.x,
                            pos.y + this.getBbHeight() / 2,  // Center of entity
                            pos.z,
                            particleNumber,  // Count
                            0, 0, 0,
                            2  // Speed multiplier
                    );
                }
                else if(this.spawnTime==half_spawn_time)
                {
                    this.playSound(this.getSpawnSound(),10,getVoicePitch());
                    level.sendParticles(
                            new DustExplosionParticleOptions(new Vec3(1, 1, 1),1.5f),
                            pos.x,
                            pos.y + this.getBbHeight() / 2,  // Center of entity
                            pos.z,
                            50,  // Count
                            0, 0, 0,
                            1  // Speed multiplier
                    );
                }

                if (this.spawnTime <= 0) {
                    this.entityData.set(IS_SPAWNING, false);
                }
            }
            else
            {
                //play black dust particle
                level.sendParticles(
                        new DustParticleOptions(0,1.0f),
                        pos.x,
                        pos.y + this.getBbHeight() / 2,  // Center of entity
                        pos.z,
                        2,  // Count
                        0.5f, 0.5f, 0.5f,
                        2  // Speed multiplier
                );
            }

            // Update HAS_TARGET value on server side
            this.entityData.set(HAS_TARGET, this.getTarget() != null);
        }


    }
    @Override
    public void aiStep()
    {
        super.aiStep();
        updateSwingTime();
    }
    @Override
    public int getCurrentSwingDuration() {
        if (MobEffectUtil.hasDigSpeed(this)) {
            return 12 - (1 + MobEffectUtil.getDigSpeedAmplification(this));
        } else {
            return this.hasEffect(MobEffects.MINING_FATIGUE) ? 12 + (1 + this.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2 : 12;
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this,level);
    }


    @Override
    protected void registerGoals() {
        //goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new FragmentAttackGoal(this, 1.0,true));
        //goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        //this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));

        this.targetSelector.addGoal(1, new NoWallCheckAttackableGoal<>(this, ServerPlayer.class, false));
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return !(source.getEntity() instanceof Player) && !source.isCreativePlayer() && !source.is(DamageTypes.GENERIC_KILL); // Can be killed by a player
// Default invulnerability for other sources
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return false;
    }
    //bypasses rage from other mobs by setting ghost as ally.

    @Override
    protected boolean considersEntityAsAlly(Entity entity) {
        return entity.getType()!=EntityType.PLAYER;
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return QuietusSounds.PLAYER_FRAGMENT_HURT.value();
    }
    @Override
    protected SoundEvent getDeathSound() {
        return QuietusSounds.PLAYER_FRAGMENT_DEATH.value();
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return QuietusSounds.PLAYER_FRAGMENT_AMBIENCE.value();
    }

    protected SoundEvent getPreSpawnSound()
    {
        return QuietusSounds.PLAYER_FRAGMENT_PRE_SPAWN.value();
    }
    protected SoundEvent getSpawnSound()
    {
        return QuietusSounds.PLAYER_FRAGMENT_SPAWN.value();
    }

    @Override
    public boolean doHurtTarget(ServerLevel p_376574_, Entity p_219472_) {
        this.playSound(QuietusSounds.PLAYER_FRAGMENT_ATTACK.value(), 10.0F, this.getVoicePitch());
        return super.doHurtTarget(p_376574_, p_219472_);
    }



    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel serverLevel) {
            // Transfer loot to killer if it's a player
            if (source.getEntity() instanceof ServerPlayer player) {
                //notification
                Component notification = Component.translatable("message.quietus.killed_fragment",player.getName()).withStyle(ChatFormatting.GREEN);
                player.sendSystemMessage(notification);

                //visual
                Vec3 pos = this.position();
                serverLevel.sendParticles(
                        ParticleTypes.FLASH,
                        pos.x,
                        pos.y + this.getBbHeight() / 2,  // Center of entity
                        pos.z,
                        1,  // Count
                        0, 0, 0,
                        1  // Speed multiplier
                );

                //retrieve stuffs
                player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,10.0f,1);
                player.giveExperiencePoints(this.storedExperience);
                boolean has_warned_full_inventory=false;
                Component full_inventory_warning = Component.translatable("message.quietus.full_inventory",player.getName()).withStyle(ChatFormatting.RED);
                for (ItemStack stack : this.lootInventory.getItems()) {
                    // Try to add the stack to player's inventory
                    if (!player.getInventory().add(stack) && !stack.isEmpty()) {
                        if(!has_warned_full_inventory)
                        {
                            player.sendSystemMessage(full_inventory_warning);
                        }
                        has_warned_full_inventory=true;
                        this.spawnAtLocation((ServerLevel) this.level(),stack); // Drop if inventory is full
                    }

                }
                this.lootInventory.clearContent();
            } else {
                // Drop all loot if killed by non-player
                for (ItemStack stack : this.lootInventory.getItems()) {
                    this.spawnAtLocation((ServerLevel) this.level(),stack);
                }
                this.lootInventory.clearContent();
            }
        }
    }
    //instantly disappear, no death time
    @Override
    protected void tickDeath() {
        if ( !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }
    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource damageSource, float amount)
    {
        super.actuallyHurt(level,damageSource,amount);
        // Get entity position
        Vec3 pos = this.position();

        // Create black particle options (using white)
        DustExplosionParticleOptions particleOptions = new DustExplosionParticleOptions(
                new Vec3(1, 1, 1), // white color
                1.0f // Scale
        );

        // Spawn particles in an explosion pattern


            // Send particles to all players
            level.sendParticles(
                    particleOptions,
                    pos.x,
                    pos.y + this.getBbHeight() / 2,  // Center of entity
                    pos.z,
                    50,  // Count
                    0, 0, 0,
                    0.5  // Speed multiplier
            );
    }


    //hit effect
// Add method to get white overlay progress (similar to CreeperRenderer)
    public float getWhiteOverlayProgress()
    {
        if (this.hurtTime <= 0) return 0.0f;

        // Similar to creeper's flashing effect
        return (float) this.hurtTime / this.hurtDuration;
    }

    // Helper method to check if entity is currently hurt
    public boolean isHurt() {
        return this.hurtTime > 0;
    }

//animation
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("player_fragment.animation", 5, this::predicate));
    }

    private PlayState predicate(AnimationTest<GeoAnimatable> animTest) {
        boolean hasTarget = this.entityData.get(HAS_TARGET);
        // Play spawn animation first

        if (this.isSpawning()) {
            animTest.controller().transitionLength(0);
            return animTest.setAndContinue(SPAWN_ANIM);
        }


        if (animTest.getDataOrDefault(DataTickets.SWINGING_ARM, false))
        {
            animTest.controller().transitionLength(2);
            return animTest.setAndContinue(ATTACK_ANIM);
        }
        animTest.controller().transitionLength(5);


        if (animTest.isMoving()&& hasTarget)
            return animTest.setAndContinue(FLY_ANIM);

        return animTest.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    class FragmentAttackGoal extends MeleeAttackGoal {
        private final PathfinderMob mob;
        private int attackCooldown;

        public FragmentAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob,speedModifier,followingTargetEvenIfNotSeen);
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.mob.getTarget() != null && !((PlayerFragment) this.mob).isSpawning();
        }

        @Override
        public boolean canContinueToUse() {
            // Continue as long as there's a target and it's alive
            return this.mob.getTarget() != null && this.mob.getTarget().isAlive() && !((PlayerFragment) this.mob).isSpawning();
        }
        @Override
        public void start()
        {
            this.attackCooldown = 0;
            this.mob.setAggressive(true);
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return;
            }

            // Always look at the target
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

            this.attackCooldown = Math.max(this.attackCooldown - 1, 0);

            this.checkAndPerformAttack(target);

            // --- MOVEMENT LOGIC ---
            // Move directly towards the target, ignoring pathfinding
            Vec3 targetPos = target.position();
            PlayerFragment.this.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, 0.8D);

        }
        @Override
        protected void resetAttackCooldown() {
            this.attackCooldown = this.adjustedTickDelay(20);
        }
        @Override
        protected boolean isTimeToAttack() {
            return this.attackCooldown <= 0;
        }
        @Override
        protected int getTicksUntilNextAttack() {
            return this.attackCooldown;
        }
    }
}
