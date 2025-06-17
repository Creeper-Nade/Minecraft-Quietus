package com.minecraftquietus.quietus.entity.ai.goal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.minecraftquietus.quietus.entity.monster.VolleyRangedAttackMob;
import com.minecraftquietus.quietus.item.weapons.QuietusProjectileWeaponItem;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
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
        if (target != null) {
            double dist_target_sqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            // TODO
            boolean has_sight = this.mob.getSensing().hasLineOfSight(target);
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
                    double d = 3.0d; // maximum distance for this to shooting straight instead
                    Vec3 p = new Vec3(
                        this.mob.getEyePosition().x,
                        this.mob.getEyePosition().y,
                        this.mob.getEyePosition().z
                    );
                    Vec3 l = new Vec3(
                        target.position().x - p.x,
                        (target.position().y + target.getEyePosition().y*3) / 4 - p.y,
                        target.position().z - p.z
                    );
                    double dist_target = Math.sqrt(dist_target_sqr);
                    double gravity = 0.05d;
                    double v0x = 0.0d; double v0ySqr = 0.0d; double v0z = 0.0d;
                    for (double i = 0.0d; i < 15.0d; i += 0.25d) {
                        double turning_point = l.y+1.5d+dist_target/4 + i;
                        double time = Math.sqrt(2.0d/gravity)*(Math.sqrt(turning_point)+Math.sqrt(turning_point-l.y)); // descending hit
                        v0ySqr = 2*turning_point*gravity;
                        /* double time = dist_target_sqr < d*d ? Math.sqrt(2.0d/gravity)*(Math.sqrt(turning_point)-Math.sqrt(turning_point-l.y)) : // ascending hit
                                Math.sqrt(2.0d/gravity)*(Math.sqrt(turning_point)+Math.sqrt(turning_point-l.y)); // descending hit */
                        
                            
                            /* double time = l.y == 0 ? 
                            (double)Math.round(2*Math.sqrt(2*max_height/gravity)) : // when relative y of target is 0 simplified
                            (double)Math.round(Math.sqrt(2.0d/gravity)*(Math.sqrt(max_height)+Math.sqrt(max_height-l.y))); */
                        // attempts to predict target movement by adding their movement change to their position
                        Vec3 target_dm = target.getKnownMovement();
                        target_dm = target_dm.add(0.0d,target.onGround() ? -(target_dm.y) : -(target_dm.y/2),0.0d); // decrease y factor for sake of against player jumping 
                        l = l.add(target_dm.scale(time));
                        // v0x and v0z calculation
                        v0x = l.x/time/0.9d; //divide by 0.9d for some reason
                        v0z = l.z/time/0.9d; //divide by 0.9d for some reason
                        if (isParabolaClear(this.mob.level(), time, v0x, Math.sqrt(v0ySqr), v0z, p, gravity)) {
                            break;
                        }
                    }
                    // calculate vector to face towards
                    Vec3 v0 = new Vec3(v0x, Math.sqrt(v0ySqr), v0z);
                    //this.mob.lookAt(Anchor.EYES, this.mob.getEyePosition().add(v0));
                    Vec3 shooter_dm = this.mob.getKnownMovement();
                    shooter_dm = shooter_dm.add(0.0d, this.mob.onGround() ? -(shooter_dm.y) : 0.0d,0.0d);
                    this.mob.lookAt(Anchor.EYES, p.add(v0).add(shooter_dm.reverse()));
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

    private static boolean isParabolaClear(Level level, double time, double v0x, double v0y, double v0z, Vec3 initialPos, double gravity) {
        for (double t = 0.0d; t <= time; t += 0.25d) {
            BlockPos pos = BlockPos.containing(
                initialPos.x + v0x * t, 
                initialPos.y + (v0y-0.5*gravity*t) * t, 
                initialPos.z + v0z * t);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    private static Map<Double,Double> searchAllPossibleParabola(double gravity, Vec3 initialPos, Vec3 targetPos, double minHeight, double maxSearchHeight, double step) {
        Map<Double,Double> turningPointToTime = new HashMap<>((int)Math.ceil((maxSearchHeight-minHeight)/step));
        Vec3 dist = targetPos.add(initialPos.reverse());
        for (double i = minHeight; i <= maxSearchHeight; i += step) {
            double turning_point = i;
            double time = Math.sqrt(2.0d/gravity)*(Math.sqrt(turning_point)+Math.sqrt(turning_point-dist.y)); // descending hit
            double v0y = Math.sqrt(2 * turning_point * gravity);
            double v0x = dist.x / time;
            double v0z = dist.z / time;
        }
        return null;
    }
}
