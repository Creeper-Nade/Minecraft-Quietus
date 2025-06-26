package com.minecraftquietus.quietus.entity.ai.goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.minecraftquietus.quietus.entity.monster.VolleyRangedAttackMob;
import com.minecraftquietus.quietus.item.weapons.QuietusProjectileWeaponItem;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ParabolaAttackGoal<T extends net.minecraft.world.entity.Mob & RangedAttackMob> extends Goal {
    private final T mob;
    private ItemStack weapon;
    private final double speedModifier;
    private int minAttackInterval;
    private final float attackRadiusSqr;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public <M extends Monster & VolleyRangedAttackMob> ParabolaAttackGoal(M mob, double speedModifier, int attackIntervalMin, float attackRadius) {
        this((T) mob, speedModifier, attackIntervalMin, attackRadius);
    }

    public ParabolaAttackGoal(T mob, double speedModifier, int attackInterval, float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.minAttackInterval = attackInterval;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void setMinAttackInterval(int attackCooldown) {
        this.minAttackInterval = attackCooldown;
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() == null ? false : true;
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone());
    }

    @Override
    public void start() {
        super.start();
        this.weapon = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof ProjectileWeaponItem));
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
        double gravity = 0.05d;
        double friction = 0.99d;
        if (target != null) {
            Vec3 targetPos = target.position();
            Vec3 targetPosToHit = new Vec3(targetPos.x, (targetPos.y+target.getEyePosition().y*3)/4, targetPos.z);
            Vec3 mobPos = this.mob.position();
            Vec3 mobPosShootFrom = this.mob.getEyePosition();
            Vec3 l = targetPosToHit.add(mobPosShootFrom.reverse());
            double minHeight = Math.max(1.5d, l.y + 0.5d);
            double maxSearchHeight = Math.max(15.0d, l.y + 20.0d);
            double dist_target_sqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            boolean has_sight = !(searchAllPossibleParabola(gravity, mobPosShootFrom, targetPosToHit, target.getHitbox(), friction, minHeight, maxSearchHeight, 0.5, this.mob.level()).isEmpty());
            /* boolean has_sight = this.mob.getSensing().hasLineOfSight(target) ? true : 
                searchAllPossibleParabola(gravity, mobPosShootFrom, targetPosToHit, target.getHitbox(), friction, minHeight, maxSearchHeight, 0.5, this.mob.level()).isEmpty() ? false : true; */
            boolean has_sight_last = this.seeTime > 0;
            if (has_sight != has_sight_last) {
                this.seeTime = 0;
            }

            if (has_sight) {
                this.seeTime++;
            } else {
                this.seeTime--;
            }

            if (!(dist_target_sqr > this.attackRadiusSqr) && this.seeTime >= 20) {
                this.mob.getNavigation().stop();
                this.strafingTime++;
            } else {
                this.mob.getNavigation().moveTo(target, this.speedModifier);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                /* if (this.mob.getRandom().nextFloat() < 0.3) {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if (this.mob.getRandom().nextFloat() < 0.3) {
                    this.strafingBackwards = !this.strafingBackwards;
                } */

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (dist_target_sqr > this.attackRadiusSqr * 0.5F) {
                    this.strafingBackwards = false;
                } else if (dist_target_sqr < this.attackRadiusSqr * 0.5F) {
                    this.strafingBackwards = true;
                }

                this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, 0.0F/* this.strafingClockwise ? 0.5F : -0.5F */);
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
                } else if (has_sight) {
                    /**
                     * t = V0y/g ± √(2g(H-y))/g = √(2/g)*(√H±√(H-y))/g
                     */
                    RandomSource random = this.mob.getRandom();
                    double dist_target = Math.sqrt(dist_target_sqr);
                    double turning_point = 0.0d;
                    double v0x = 0.0d; double v0y = 0.0d; double v0z = 0.0d;
                    double time = 0.0d;
                    List<Double[]> parabolaList = searchAllPossibleParabola(gravity, mobPosShootFrom, targetPosToHit, target.getHitbox(), friction, minHeight, maxSearchHeight, 0.25d, this.mob.level());
                    int size = parabolaList.size();
                    int size_half = (int)Math.round((size+1)/2.0d);
                    Double[] parabola = size_half == size || size_half == 0 ? parabolaList.get(0) :  // avoid bound-origin is non positive
                        parabolaList.get(
                            random.nextInt(size_half, size) - random.nextInt(0, size_half)
                        );
                    turning_point = parabola[0];
                    time = parabola[1];
                    v0x = parabola[2];
                    v0y = parabola[3];
                    v0z = parabola[4];
                    
                    // calculate vector to face towards
                    Vec3 v0 = new Vec3(v0x, v0y, v0z);
                    // apply reversed this mob's movement to negate the projecitle intertia due to shooter
                    Vec3 shooter_dm = this.mob.getKnownMovement();
                    shooter_dm = shooter_dm.add(0.0d, this.mob.onGround() ? -(shooter_dm.y) : 0.0d, 0.0d);
                    this.mob.lookAt(Anchor.EYES, mobPosShootFrom.add(v0).add(shooter_dm.reverse()));
                    int useTicks = this.mob.getTicksUsingItem();
                    if (this.weapon.getItem() instanceof BowItem) {
                        if (useTicks >= 10) {
                            this.mob.stopUsingItem();
                            this.mob.performRangedAttack(target, BowItem.getPowerForTime(useTicks*2)*(float)v0.length());
                            this.attackTime = this.minAttackInterval;
                        }
                    } else if (this.weapon.getItem() instanceof QuietusProjectileWeaponItem weapon_item && weapon_item.getUseDuration(weapon, this.mob) > 0 && weapon_item.getPowerDuration(weapon, this.mob) >= 0) {
                        if (useTicks >= (weapon_item.getPowerDuration(weapon, this.mob))/2) {
                            /* System.out.println("x: "+ v0.x + " | y: " + v0.y + " | z: " + v0.z);
                            System.out.println(dist_target + " | " + time + " | " + target.position().x + " " + target.position().y + " "+ target.position().z); */
                            this.mob.stopUsingItem();
                            this.mob.performRangedAttack(target, weapon_item.getPowerForTime(useTicks*2)*(float)v0.length());
                            this.attackTime = this.minAttackInterval;
                        }
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof ProjectileWeaponItem));
            }
        }
    }

    private static boolean isParabolaClear(double gravity, Vec3 initialPos, double friction, Level level, double time, Vec3 v0) {
        Vec3 v = v0;
        Vec3 p = initialPos;

        for (int t = 0; t <= (int)Math.ceil(time); t += 1) {
            BlockPos pos = BlockPos.containing(p.x, p.y, p.z);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return false;
            }
            p = p.add(v);
            v = v.add(0.0d,-gravity,0.0d);
            v = v.scale(friction);
        }
        return true;
    }
    /**
     * Search for all possible parabolas
     * @param gravity
     * @param initialPos
     * @param targetPos
     * @param targetHitBox
     * @param friction
     * @param minHeight
     * @param maxSearchHeight
     * @param step
     * @param level
     * @return list of arrays of 5 doubles respectively being: turning_point (relative to initialPos), time, v0x, v0y, v0z
     */
    /* private static List<Double[]> searchAllPossibleParabola(double gravity, Vec3 initialPos, Vec3 targetPos, AABB targetHitBox, double friction, double minHeight, double maxSearchHeight, double step, Level level) {
        List<Double[]> out = new ArrayList<>();
        Vec3 dist = targetPos.subtract(initialPos);
        for (double i = minHeight; i <= maxSearchHeight; i += step) {
            double turning_point = i;
            final double const_1 = (gravity*friction) / (friction-1.0d); // the minimum vy possible (noted as f*)
            double time = turning_point / const_1 + 1 / Math.log(friction);
            double v0y = (1.0d - 1.0d/Math.pow(friction,time)) * const_1;
            double v0x = dist.x / time / Math.pow(friction, time);
            double v0z = dist.z / time / Math.pow(friction, time);
            System.out.println(time);
            Vec3 v = new Vec3(v0x, v0y, v0z);
            if (isParabolaClear(gravity, initialPos, friction, level, time, v)) {
                Double[] coll = {turning_point,time,v0x,v0y,v0z};
                out.add(coll);
            }
        }
        return out;
    } */
    private static List<Double[]> searchAllPossibleParabola(double gravity, Vec3 initialPos, Vec3 targetPos, AABB targetHitBox, double friction, double minHeight, double maxSearchHeight, double step, Level level) {
        List<Double[]> out = new ArrayList<>();
        Vec3 dist = targetPos.add(initialPos.reverse());
        for (double i = minHeight; i <= maxSearchHeight; i += step) {
            double turning_point = i;
            double time = Math.sqrt(2.0d/gravity)*(Math.sqrt(turning_point)+Math.sqrt(turning_point-dist.y)); // descending hit
            double v0y = Math.sqrt(2 * turning_point * gravity);
            double v0x = dist.x / time / 0.9d; // 0.9d is imperical factor, do not change
            double v0z = dist.z / time / 0.9d; // 0.9d is imperical factor, do not change
            Vec3 p = initialPos;
            Vec3 v = new Vec3(v0x, v0y, v0z);
            if (isParabolaClear(gravity, initialPos, friction, level, time, v)) {
                boolean hit = false;
                for (int t = 0; t <= (int)Math.ceil(time); t += 1) {
                    p = p.add(v);
                    v = v.add(0.0d,-gravity,0.0d);
                    v = v.scale(friction);
                    if (targetHitBox.contains(p)) {
                        hit = true;
                    }
                }
                if (hit) {
                    Double[] coll = {turning_point,time,v0x,v0y,v0z};
                        out.add(coll);
                }
            }
        }
        return out;
    }
}
