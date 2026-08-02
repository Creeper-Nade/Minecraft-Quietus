package com.quietus.mixin;

import com.quietus.combat.AttackImmunitySystem;
import com.quietus.combat.AttackInstance;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public abstract class DamageSourceMixin implements AttackInstance {
    @Unique
    private long quietus$attackId = AttackImmunitySystem.nextAttackId();

    @Unique
    private int quietus$immunityTicks = -1;

    @Override
    public long quietus$getAttackId() {
        return this.quietus$attackId;
    }

    @Override
    public void quietus$setAttackId(long attackId) {
        this.quietus$attackId = attackId;
    }

    @Override
    public int quietus$getImmunityTicks() {
        return this.quietus$immunityTicks;
    }

    @Override
    public void quietus$setImmunityTicks(int ticks) {
        this.quietus$immunityTicks = ticks;
    }
}
