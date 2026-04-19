package com.minecraftquietus.quietus.entity.ai.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class NoWallCheckAttackableGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final TargetingConditions.Selector selector;

    public NoWallCheckAttackableGoal(Mob mob, Class<T> targetType, boolean mustSee) {
        this(mob, targetType, 10, mustSee, false, null);
    }

    public NoWallCheckAttackableGoal(Mob mob, Class<T> targetType, int interval, boolean mustSee, boolean mustReach, @Nullable TargetingConditions.Selector selector) {
        // mustSee is passed to super, but we will bypass its effect in findTarget
        super(mob, targetType, interval, mustSee, mustReach, selector);
        this.selector = selector;
    }

    @Override
    protected void findTarget() {
        ServerLevel level = getServerLevel(this.mob);

        // Build custom conditions similar to Pufferfish's static conditions
        TargetingConditions conditions = TargetingConditions.forCombat()
                .range(this.getFollowDistance())
                .selector(this.selector)
                .ignoreLineOfSight(); // This bypasses the wall check

        // Replicate the search logic from NearestAttackableTargetGoal [cite: 92, 93]
        if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
            this.target = level.getNearestEntity(
                    this.mob.level().getEntitiesOfClass(
                            this.targetType,
                            this.getTargetSearchArea(this.getFollowDistance()),
                            (entity) -> true
                    ),
                    conditions,
                    this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()
            );
        } else {
            this.target = level.getNearestPlayer(
                    conditions,
                    this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()
            );
        }
    }
}
