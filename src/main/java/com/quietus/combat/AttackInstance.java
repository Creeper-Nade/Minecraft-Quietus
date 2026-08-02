package com.quietus.combat;

/**
 * Runtime metadata attached to every {@code DamageSource} by Quietus.
 *
 * <p>The id identifies one logical attack. The cooldown override is optional;
 * negative values select the default policy in {@link AttackImmunitySystem}.</p>
 */
public interface AttackInstance {
    long quietus$getAttackId();

    void quietus$setAttackId(long attackId);

    int quietus$getImmunityTicks();

    void quietus$setImmunityTicks(int ticks);
}
