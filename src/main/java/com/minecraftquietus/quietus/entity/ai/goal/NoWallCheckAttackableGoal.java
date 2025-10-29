package com.minecraftquietus.quietus.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class NoWallCheckAttackableGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public NoWallCheckAttackableGoal(Mob mob, Class<T> targetType, boolean mustSee) {
        this(mob, targetType, 10, mustSee, false, (TargetingConditions.Selector)null);
    }

    public NoWallCheckAttackableGoal(Mob mob, Class<T> targetType, boolean mustSee, TargetingConditions.Selector selector) {
        this(mob, targetType, 10, mustSee, false, selector);
    }

    public NoWallCheckAttackableGoal(Mob mob, Class<T> targetType, boolean mustSee, boolean mustReach) {
        this(mob, targetType, 10, mustSee, mustReach, (TargetingConditions.Selector)null);
    }
    public NoWallCheckAttackableGoal(Mob mob, Class<T> targetType, int interval, boolean mustSee, boolean mustReach, @Nullable TargetingConditions.Selector selector) {
        super(mob, targetType, mustSee);
        this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(selector).ignoreLineOfSight();
    }
}
