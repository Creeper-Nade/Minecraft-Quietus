package com.quietus.combat;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;

/** Utilities and default balancing policy for per-attack invincibility frames. */
public final class AttackImmunitySystem {
    public static final int PROJECTILE_IMMUNITY_TICKS = 5;
    public static final int ATTACK_IMMUNITY_TICKS = 10;
    public static final int FIRE_IMMUNITY_TICKS = 20;

    private static final AtomicLong NEXT_ATTACK_ID = new AtomicLong();

    private AttackImmunitySystem() {
    }

    public static long nextAttackId() {
        return NEXT_ATTACK_ID.incrementAndGet();
    }

    /**
     * Overrides the cooldown for a reusable attack such as a beam or persistent
     * area. Zero allows every damage attempt; a negative value restores the
     * default policy.
     */
    public static DamageSource setImmunityTicks(DamageSource source, int ticks) {
        ((AttackInstance) source).quietus$setImmunityTicks(ticks);
        return source;
    }

    /** Makes two separately-created damage sources represent the same attack. */
    public static DamageSource shareAttackId(DamageSource source, DamageSource attackToShare) {
        AttackInstance sourceInstance = (AttackInstance) source;
        AttackInstance sharedInstance = (AttackInstance) attackToShare;
        sourceInstance.quietus$setAttackId(sharedInstance.quietus$getAttackId());
        return source;
    }

    public static AttackKey key(DamageSource source) {
        Entity directEntity = source.getDirectEntity();

        // A projectile or persistent non-living attack entity remains the same
        // logical attack even when it creates a fresh DamageSource on every hit.
        if (directEntity instanceof Projectile || directEntity != null && !(directEntity instanceof LivingEntity)) {
            return AttackKey.forEntity(directEntity.getUUID());
        }

        // Cached and freshly-created environmental sources both need the same
        // stable key. Keeping damage types separate lets lava, fire, drowning,
        // and similar hazards retain independent timers.
        if (directEntity == null && source.getEntity() == null) {
            return AttackKey.forEnvironment(source.getMsgId());
        }

        // Melee and other entity-caused attacks normally allocate one source per
        // swing. Reusing that source naturally shares the id across an AOE hitbox.
        return AttackKey.forDamageSource(((AttackInstance) source).quietus$getAttackId());
    }

    public static int immunityTicks(DamageSource source) {
        int override = ((AttackInstance) source).quietus$getImmunityTicks();
        if (override >= 0) {
            return override;
        }
        if (source.getDirectEntity() instanceof Projectile) {
            return PROJECTILE_IMMUNITY_TICKS;
        }
        if (source.is(DamageTypes.ON_FIRE)) {
            return FIRE_IMMUNITY_TICKS;
        }
        return ATTACK_IMMUNITY_TICKS;
    }

    public record AttackKey(Kind kind, UUID entityId, String damageType, long attackId) {
        private static AttackKey forEntity(UUID entityId) {
            return new AttackKey(Kind.ENTITY, entityId, "", 0L);
        }

        private static AttackKey forEnvironment(String damageType) {
            return new AttackKey(Kind.ENVIRONMENT, null, damageType, 0L);
        }

        private static AttackKey forDamageSource(long attackId) {
            return new AttackKey(Kind.DAMAGE_SOURCE, null, "", attackId);
        }
    }

    public enum Kind {
        ENTITY,
        ENVIRONMENT,
        DAMAGE_SOURCE
    }
}
