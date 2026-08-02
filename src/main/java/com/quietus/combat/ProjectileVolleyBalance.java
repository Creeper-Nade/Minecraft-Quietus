package com.quietus.combat;

import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

/**
 * Gives simultaneous projectile volleys a capped total damage budget.
 *
 * <p>By default, each extra projectile contributes 35% of a single shot, up to 250% total
 * damage. Knockback is divided evenly, so landing an entire volley cannot apply
 * more base knockback than one projectile.</p>
 */
public final class ProjectileVolleyBalance {
    public static final float EXTRA_PROJECTILE_DAMAGE_CONTRIBUTION = 0.35F;
    /** Compensates the Paraboler's deliberately slower arrows and attack cadence. */
    public static final float PARABOLER_EXTRA_PROJECTILE_DAMAGE_CONTRIBUTION = 0.60F;
    /** Raw arrow damage targeted before the Paraboler's three-projectile budget is applied. */
    public static final float PARABOLER_PRE_VOLLEY_ARROW_DAMAGE = 3.5F;
    public static final float MAX_TOTAL_DAMAGE_MULTIPLIER = 2.5F;

    private ProjectileVolleyBalance() {
    }

    public static int countProjectiles(List<ItemStack> projectileItems) {
        int count = 0;
        for (ItemStack projectileItem : projectileItems) {
            if (!projectileItem.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public static float damageScale(int projectileCount) {
        return damageScale(projectileCount, EXTRA_PROJECTILE_DAMAGE_CONTRIBUTION);
    }

    public static float damageScale(int projectileCount, float extraProjectileContribution) {
        if (projectileCount <= 1) {
            return 1.0F;
        }
        float totalDamageMultiplier = Math.min(
                MAX_TOTAL_DAMAGE_MULTIPLIER,
                1.0F + extraProjectileContribution * (projectileCount - 1)
        );
        return totalDamageMultiplier / projectileCount;
    }

    public static float knockbackScale(int projectileCount) {
        return projectileCount <= 1 ? 1.0F : 1.0F / projectileCount;
    }

    public static <T extends Projectile> T apply(T projectile, int projectileCount) {
        return apply(projectile, projectileCount, EXTRA_PROJECTILE_DAMAGE_CONTRIBUTION);
    }

    public static <T extends Projectile> T apply(T projectile, int projectileCount, float extraProjectileContribution) {
        if (projectileCount > 1) {
            VolleyBalancedProjectile balanced = (VolleyBalancedProjectile) projectile;
            balanced.quietus$setVolleySize(projectileCount);
            balanced.quietus$setVolleyDamageScale(damageScale(projectileCount, extraProjectileContribution));
            balanced.quietus$setVolleyKnockbackScale(knockbackScale(projectileCount));
        }
        return projectile;
    }

    public static float damageScale(Entity directEntity) {
        return directEntity instanceof VolleyBalancedProjectile balanced
                ? balanced.quietus$getVolleyDamageScale()
                : 1.0F;
    }

    public static float knockbackScale(Entity directEntity) {
        return directEntity instanceof VolleyBalancedProjectile balanced
                ? balanced.quietus$getVolleyKnockbackScale()
                : 1.0F;
    }
}
