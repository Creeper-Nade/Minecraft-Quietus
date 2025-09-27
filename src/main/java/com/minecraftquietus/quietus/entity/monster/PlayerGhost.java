package com.minecraftquietus.quietus.entity.monster;

import com.minecraftquietus.quietus.client.model.mob.PlayerGhostRenderer;
import com.minecraftquietus.quietus.client.particle.particle_options.DustExplosionParticleOptions;
import com.minecraftquietus.quietus.entity.QuietusEntityDataSerializers;
import com.minecraftquietus.quietus.entity.ai.goal.GhostAttackGoal;
import com.minecraftquietus.quietus.entity.ai.goal.NoWallCheckAttackableGoal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FLYING_SPEED, 0.8D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    public void setPlayerHeadTexture(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation headTex=minecraft.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
        entityData.set(HeadTexture, headTex);
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
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            // Transfer loot to killer if it's a player
            if (source.getEntity() instanceof Player player) {
                for (ItemStack stack : this.lootInventory.getItems()) {
                    if (!player.getInventory().add(stack)) {
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
