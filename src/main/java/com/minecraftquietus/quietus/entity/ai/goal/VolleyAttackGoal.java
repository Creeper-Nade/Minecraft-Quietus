package com.minecraftquietus.quietus.entity.ai.goal;

import java.util.EnumSet;

import com.minecraftquietus.quietus.entity.monster.VolleyRangedAttackMob;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;

public class VolleyAttackGoal<T extends net.minecraft.world.entity.Mob & VolleyRangedAttackMob> extends Goal {
    private final T mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private int volleyMax;
    private final float attackRadiusSqr;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public <M extends Monster & VolleyRangedAttackMob> VolleyAttackGoal(M mob, double speedModifier, int attackIntervalMin, float attackRadius) {
        this((T) mob, speedModifier, attackIntervalMin, attackRadius);
    }

    public VolleyAttackGoal(T mob, double speedModifier, int attackIntervalMin, float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.volleyMax = this.mob.getVolleyMax();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void setMinAttackInterval(int attackCooldown) {
        this.attackIntervalMin = attackCooldown;
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() == null ? false : this.isHoldingDuelBow();
    }

    protected boolean isHoldingDuelBow() {
        return (this.mob.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BowItem);
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingDuelBow();
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        int volleyAmount = this.mob.getVolley();
        if (target != null) {
            double dist_target = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            boolean has_sight = this.mob.getSensing().hasLineOfSight(target);
            boolean has_sight_last = this.seeTime > 0;
            InteractionHand hand_last = this.mob.getUsedItemHand();
            if (has_sight != has_sight_last) {
                this.seeTime = 0;
            }

            if (has_sight) {
                this.seeTime++;
            } else {
                this.seeTime--;
                this.mob.setVolley(0);
            }

            if (!(dist_target > this.attackRadiusSqr) && this.seeTime >= 20) {
                this.mob.getNavigation().stop();
                this.strafingTime++;
            } else {
                this.mob.getNavigation().moveTo(target, this.speedModifier);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if (this.mob.getRandom().nextFloat() < 0.3) {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if (this.mob.getRandom().nextFloat() < 0.3) {
                    this.strafingBackwards = !this.strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (dist_target > this.attackRadiusSqr * 0.5F) {
                    this.strafingBackwards = false;
                } else if (dist_target < this.attackRadiusSqr * 0.5F) {
                    this.strafingBackwards = true;
                }

                this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                if (this.mob.getControlledVehicle() instanceof Mob mob) {
                    mob.lookAt(target, 30.0F, 30.0F);
                }

                this.mob.lookAt(target, 30.0F, 30.0F);
            } else {
                this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }

            if (this.mob.isUsingItem()) {
                if (!has_sight && this.seeTime < -60) { // lost sight
                    this.mob.stopUsingItem();
                    this.mob.setVolley(0);
                } else if (has_sight) {
                    int useTicks = this.mob.getTicksUsingItem();
                    if (useTicks >= 7) {
                        this.mob.stopUsingItem();
                        this.mob.setVolley(volleyAmount+1);
                        this.mob.performRangedAttack(target, BowItem.getPowerForTime(useTicks*2));
                        if (volleyAmount >= volleyMax) {
                            this.attackTime = this.attackIntervalMin;
                            this.mob.setVolley(0);
                        }
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                if (hand_last == InteractionHand.OFF_HAND) {
                    this.mob.startUsingItem(InteractionHand.MAIN_HAND);
                } else {
                    this.mob.startUsingItem(InteractionHand.OFF_HAND);
                }
            }
        }
    }
}
