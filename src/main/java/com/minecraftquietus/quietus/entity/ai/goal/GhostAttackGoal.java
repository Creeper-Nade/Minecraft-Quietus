package com.minecraftquietus.quietus.entity.ai.goal;

import com.minecraftquietus.quietus.entity.monster.PlayerGhost;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GhostAttackGoal extends Goal {
    private final PathfinderMob mob;
    private final double speedModifier;
    private int attackCooldown;

    public GhostAttackGoal(PathfinderMob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
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
    public void stop() {
        // Stop movement when the goal ends
        this.mob.getNavigation().stop();
        this.mob.setDeltaMovement(Vec3.ZERO);
        super.stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }

        // Always look at the target
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distanceToTargetSq = this.mob.distanceToSqr(target);

        // --- ATTACK LOGIC ---
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        double attackReachSq = this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + target.getBbWidth();
        if (distanceToTargetSq <= attackReachSq) {
            if (this.attackCooldown <= 0) {
                this.attackCooldown = 20; // 20 ticks = 1 second cooldown
                this.mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                this.mob.doHurtTarget(getServerLevel(this.mob),target);
            }
        }

        // --- MOVEMENT LOGIC ---
        // Move directly towards the target, ignoring pathfinding
        Vec3 direction = new Vec3(target.getX() - this.mob.getX(), target.getEyeY() - this.mob.getEyeY(), target.getZ() - this.mob.getZ());
        this.mob.setDeltaMovement(direction.normalize().scale(this.speedModifier * this.mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)));
        //Vec3 targetPos = target.position();
        //PlayerGhost.this.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, 0.8D);

    }
}
