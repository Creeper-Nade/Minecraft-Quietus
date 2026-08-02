package com.quietus.mixin;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.quietus.combat.AttackImmunitySystem;
import com.quietus.combat.AttackImmunitySystem.AttackKey;
import com.quietus.combat.ProjectileVolleyBalance;
import com.quietus.util.QuietusGameRules;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityAttackImmunityMixin {
    @Unique
    private static final int QUIETUS_MAX_TRACKED_ATTACKS = 1024;

    @Unique
    private static final float QUIETUS_UNARMED_KNOCKBACK_START_CHARGE = 0.8F;

    @Unique
    private final Map<AttackKey, Long> quietus$attackImmunities = new HashMap<>();

    @Unique
    private final ArrayDeque<DamageAttempt> quietus$damageAttempts = new ArrayDeque<>();

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void quietus$beginDamageAttempt(ServerLevel level, DamageSource source, float damage,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (!QuietusGameRules.useRevampedInvulnerabilityFrames(level)) {
            this.quietus$attackImmunities.clear();
            this.quietus$damageAttempts.push(DamageAttempt.unmanaged());
            return;
        }

        // Preserve vanilla's explicit escape hatch. Sources in this tag bypass
        // both vanilla and Quietus attack cooldowns.
        if (source.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
            this.quietus$damageAttempts.push(DamageAttempt.unmanaged());
            return;
        }

        long now = level.getGameTime();
        this.quietus$removeExpired(now);
        AttackKey key = AttackImmunitySystem.key(source);
        Long expiresAt = this.quietus$attackImmunities.get(key);
        if (expiresAt != null && now < expiresAt) {
            this.quietus$damageAttempts.push(DamageAttempt.blocked());
            cir.setReturnValue(false);
            return;
        }

        int immunityTicks = AttackImmunitySystem.immunityTicks(source);
        this.quietus$damageAttempts.push(DamageAttempt.managed(key, now + immunityTicks));
    }

    @ModifyExpressionValue(
            method = "hurtServer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I",
                    ordinal = 0
            )
    )
    private int quietus$ignoreGlobalCooldownForDistinctAttack(int vanillaInvulnerableTime) {
        DamageAttempt attempt = this.quietus$damageAttempts.peek();
        // Change only the value used by `invulnerableTime > 10`; do not mutate
        // the entity field or the post-damage value maintained by NeoForge.
        return attempt != null && attempt.managed() ? 0 : vanillaInvulnerableTime;
    }

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float quietus$applyProjectileVolleyDamage(float damage,
                                                       @Local(argsOnly = true) DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!QuietusGameRules.useRevampedInvulnerabilityFrames(self.level())) {
            return damage;
        }
        return damage * ProjectileVolleyBalance.damageScale(source.getDirectEntity());
    }

    @ModifyArg(
            method = "hurtServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"
            ),
            index = 0
    )
    private double quietus$scalePlayerMeleeKnockback(double vanillaStrength,
                                                      @Local(argsOnly = true) DamageSource source,
                                                      @Local(argsOnly = true) float damage) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!QuietusGameRules.useRevampedInvulnerabilityFrames(self.level())) {
            return vanillaStrength;
        }
        float projectileScale = ProjectileVolleyBalance.knockbackScale(source.getDirectEntity());
        if (projectileScale != 1.0F) {
            return vanillaStrength * projectileScale;
        }
        if (!(source.getEntity() instanceof Player player)
                || source.getDirectEntity() != player) {
            return vanillaStrength;
        }

        // Vanilla's fixed impact was previously rate-limited by the global
        // invulnerability window. Preserve the full-charge result while making
        // rapid punches yield the same approximate knockback per second instead
        // of becoming an effortless escape tool. A hit that deals no damage
        // (for example, an unarmed player under sufficient Weakness) cannot push.
        if (damage <= 0.0F) {
            return 0.0;
        }

        float attackCharge = player.getAttackStrengthScale(0.5F);
        if (!player.getWeaponItem().has(DataComponents.WEAPON)) {
            // Fists recharge in roughly five ticks. Linear scaling alone still
            // permits many small impulses that repeatedly interrupt a mob. An
            // unarmed/non-weapon attack therefore starts gaining knockback only
            // over the final 20% of its charge: 80% -> 0, 90% -> half, 100% ->
            // vanilla. Rapid punches continue to register damage without being
            // usable as a permanent spacing tool.
            float unarmedScale = Math.max(
                    0.0F,
                    (attackCharge - QUIETUS_UNARMED_KNOCKBACK_START_CHARGE)
                            / (1.0F - QUIETUS_UNARMED_KNOCKBACK_START_CHARGE)
            );
            return vanillaStrength * Math.min(unarmedScale, 1.0F);
        }
        return vanillaStrength * attackCharge;
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void quietus$finishDamageAttempt(ServerLevel level, DamageSource source, float damage,
                                              CallbackInfoReturnable<Boolean> cir) {
        // Another mod may cancel at HEAD before this mixin's HEAD callback runs.
        // Its RETURN callback still reaches this injection point.
        if (this.quietus$damageAttempts.isEmpty()) {
            return;
        }
        DamageAttempt attempt = this.quietus$damageAttempts.pop();
        if (!attempt.managed()) {
            return;
        }

        if (cir.getReturnValue()) {
            if (attempt.expiresAt() > level.getGameTime()) {
                this.quietus$attackImmunities.put(attempt.key(), attempt.expiresAt());
                this.quietus$enforceSizeLimit();
            }
        }
    }

    @Unique
    private void quietus$removeExpired(long now) {
        this.quietus$attackImmunities.values().removeIf(expiresAt -> expiresAt <= now);
    }

    @Unique
    private void quietus$enforceSizeLimit() {
        while (this.quietus$attackImmunities.size() > QUIETUS_MAX_TRACKED_ATTACKS) {
            Iterator<Map.Entry<AttackKey, Long>> entries = this.quietus$attackImmunities.entrySet().iterator();
            Map.Entry<AttackKey, Long> earliest = null;
            while (entries.hasNext()) {
                Map.Entry<AttackKey, Long> candidate = entries.next();
                if (earliest == null || candidate.getValue() < earliest.getValue()) {
                    earliest = candidate;
                }
            }
            if (earliest != null) {
                this.quietus$attackImmunities.remove(earliest.getKey());
            }
        }
    }

    @Unique
    private record DamageAttempt(boolean managed, AttackKey key, long expiresAt) {
        private static DamageAttempt managed(AttackKey key, long expiresAt) {
            return new DamageAttempt(true, key, expiresAt);
        }

        private static DamageAttempt blocked() {
            return new DamageAttempt(false, null, 0L);
        }

        private static DamageAttempt unmanaged() {
            return new DamageAttempt(false, null, 0L);
        }
    }
}
