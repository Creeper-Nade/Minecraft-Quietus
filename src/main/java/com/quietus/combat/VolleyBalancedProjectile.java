package com.quietus.combat;

/** Runtime and save-data metadata carried by projectiles created in one volley. */
public interface VolleyBalancedProjectile {
    float quietus$getVolleyDamageScale();

    void quietus$setVolleyDamageScale(float scale);

    float quietus$getVolleyKnockbackScale();

    void quietus$setVolleyKnockbackScale(float scale);

    int quietus$getVolleySize();

    void quietus$setVolleySize(int size);
}
