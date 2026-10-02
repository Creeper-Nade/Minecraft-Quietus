package com.quietus.entity.projectiles;

import javax.annotation.Nullable;

import com.quietus.item.property.QuietusProjectileProperty;

import net.minecraft.world.item.ItemStack;

public interface IQuietusProjectile {
    public void configure(QuietusProjectileProperty projectileProperty, @Nullable ItemStack item);

    public void setCritical(boolean isCrit);

    public void rollCriticalHit();

    public void applyCastingResult(int successes, int totalChecks);
}
