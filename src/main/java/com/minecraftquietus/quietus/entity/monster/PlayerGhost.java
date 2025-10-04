package com.minecraftquietus.quietus.entity.monster;

import com.minecraftquietus.quietus.client.model.mob.PlayerGhostRenderer;
import com.minecraftquietus.quietus.client.particle.particle_options.DustExplosionParticleOptions;
import com.minecraftquietus.quietus.entity.QuietusEntityDataSerializers;
import com.minecraftquietus.quietus.entity.ai.goal.GhostAttackGoal;
import com.minecraftquietus.quietus.entity.ai.goal.NoWallCheckAttackableGoal;
import com.minecraftquietus.quietus.sounds.QuietusSounds;
import com.minecraftquietus.quietus.util.sound.EntitySoundSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class PlayerGhost extends PathfinderMob implements GeoEntity {
    //private static final EntityDataAccessor<ItemStack> HEAD_ITEM = SynchedEntityData.defineId(PlayerGhost.class, EntityDataSerializers.ITEM_STACK);
    //private ResourceLocation HeadTexture;
    private static final EntityDataAccessor<ResourceLocation> HeadTexture= SynchedEntityData.defineId(PlayerGhost.class, QuietusEntityDataSerializers.RESOURCE_LOCATION.get());
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final SimpleContainer lootInventory = new SimpleContainer(27);
    private int storedExperience; // Stores the total XP points

    private static final EntityDataAccessor<Boolean> HAS_TARGET = SynchedEntityData.defineId(PlayerGhost.class, EntityDataSerializers.BOOLEAN);



    // Animation state tracking
    //private int noTargetTicks = 0;
    //private static final int MAX_NO_TARGET_TICKS = 100;

    //declare animations
    protected static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("animation.player_ghost.walk");
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.player_ghost.idle");
    protected static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.player_ghost.attack");
    protected static final RawAnimation SPAWN_ANIM = RawAnimation.begin().thenPlay("animation.player_ghost.spawn");

    public PlayerGhost(EntityType<? extends PathfinderMob> type, Level level) {
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
            ResourceLocation savedTexture = ResourceLocation.parse(tag.getStringOr("HeadTexture","null"));
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
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    public void setPlayerData(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation headTex=minecraft.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
        entityData.set(HeadTexture, headTex);

        this.setStoredExperience(player.totalExperience);
        setPlayerName(player);
    }
    public ResourceLocation getPlayerHeadTexture()
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

    @Override
    public void tick() {
        this.noPhysics = true;
        this.setNoGravity(true);
        super.tick();
        if (!this.level().isClientSide()) {
            // Update HAS_TARGET value on server side
            this.entityData.set(HAS_TARGET, this.getTarget() != null);
        }

    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this,level);
    }


    @Override
    protected void registerGoals() {
        //goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new GhostAttackGoal(this, 1.0,true));
        //goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));


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

    @Override
    public boolean doHurtTarget(ServerLevel p_376574_, Entity p_219472_) {
        this.playSound(QuietusSounds.PLAYER_FRAGMENT_ATTACK.value(), 10.0F, this.getVoicePitch());
        return super.doHurtTarget(p_376574_, p_219472_);
    }



    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            // Transfer loot to killer if it's a player
            if (source.getEntity() instanceof ServerPlayer player) {
                //notification
                Component notification = Component.translatable("message.quietus.killed_fragment",player.getName()).withStyle(ChatFormatting.GREEN);
                player.sendSystemMessage(notification);

                //retrieve stuffs
                player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,10.0f,1);
                player.giveExperiencePoints(this.storedExperience);
                boolean has_warned_full_inventory=false;
                Component full_inventory_warning = Component.translatable("message.quietus.full_inventory",player.getName()).withStyle(ChatFormatting.RED);
                for (ItemStack stack : this.lootInventory.getItems()) {
                    // Try to add the stack to player's inventory
                    if (!player.getInventory().add(stack)) {
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
    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource damageSource, float amount)
    {
        super.actuallyHurt(level,damageSource,amount);
        // Get entity position
        Vec3 pos = this.position();

        // Create black particle options (using very dark gray)
        DustExplosionParticleOptions particleOptions = new DustExplosionParticleOptions(
                new Vec3(1, 1, 1), // Dark gray/black color
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
        controllers.add(new AnimationController<>("player_ghost.animation", 5, this::predicate));
    }

    private PlayState predicate(AnimationTest<GeoAnimatable> animTest) {
        boolean hasTarget = this.entityData.get(HAS_TARGET);
        if (animTest.isMoving()&& hasTarget)
            return animTest.setAndContinue(FLY_ANIM);

        return animTest.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    class GhostAttackGoal extends MeleeAttackGoal {
        private final PathfinderMob mob;
        private int attackCooldown;

        public GhostAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob,speedModifier,followingTargetEvenIfNotSeen);
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.mob.getTarget() != null;
        }

        @Override
        public boolean canContinueToUse() {
            // Continue as long as there's a target and it's alive
            return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
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
            PlayerGhost.this.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, 0.8D);

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
