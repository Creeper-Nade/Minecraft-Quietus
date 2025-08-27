package com.minecraftquietus.quietus.entity.monster;

import com.minecraftquietus.quietus.client.model.mob.PlayerGhostRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class PlayerGhost extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<ItemStack> HEAD_ITEM = SynchedEntityData.defineId(PlayerGhost.class, EntityDataSerializers.ITEM_STACK);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final SimpleContainer lootInventory = new SimpleContainer(27);

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
        this.moveControl = new FlyingMoveControl(this, 20, true);
        //this.noPhysics = true;
        //this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder dataBuild) {
        super.defineSynchedData(dataBuild);;
        dataBuild.define(HEAD_ITEM, ItemStack.EMPTY);
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

    public void setPlayerHead(ItemStack headStack) {
        // Ensure we don't set an empty ItemStack
        if (headStack.isEmpty()) {
            headStack = new ItemStack(Items.PLAYER_HEAD);
        }
        entityData.set(HEAD_ITEM, headStack);
    }

    public ItemStack getPlayerHead() {
        ItemStack head = entityData.get(HEAD_ITEM);
        return head.isEmpty() ? new ItemStack(Items.PLAYER_HEAD) : head;
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
        //this.noPhysics = false;
        // Handle target tracking
        /*
        if (this.getTarget() != null) {
            noTargetTicks = 0;

            // Check if target is too far or not line of sight
            if (this.distanceTo(this.getTarget()) > 32.0 || !this.hasLineOfSight(this.getTarget())) {
                this.setTarget(null);
            }
        } else {
            noTargetTicks++;

            // Find new target if none exists
            if (noTargetTicks % 20 == 0) {
                Player nearestPlayer = this.level().getNearestPlayer(this, 16.0);
                if (nearestPlayer != null && this.hasLineOfSight(nearestPlayer)) {
                    this.setTarget(nearestPlayer);
                    noTargetTicks = 0;
                }
            }
        }

        // Stop movement when no target
        if (this.getTarget() == null && this.getNavigation().isInProgress()) {
            this.getNavigation().stop();
        }*/

    }
    /*
    @Override
    public void move(MoverType type, Vec3 movement) {
        // Allow vertical movement for chasing players
        super.move(type, movement);
    }*/
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        //goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        //goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));


        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, null));
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("player_ghost.animation", 5, this::predicate));
    }

    private PlayState predicate(AnimationTest<GeoAnimatable> animTest) {
        System.out.println(this.getTarget());
        if (animTest.isMoving()&& getTarget() != null)
            return animTest.setAndContinue(FLY_ANIM);

        return animTest.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
