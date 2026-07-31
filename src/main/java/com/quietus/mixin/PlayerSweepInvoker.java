package com.quietus.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Player.class)
public interface PlayerSweepInvoker {
    @Invoker("isSweepAttack")
    boolean quietus$isSweepAttack(boolean fullStrengthAttack, boolean criticalAttack, boolean knockbackAttack);

    @Invoker("createAttackSource")
    DamageSource quietus$createAttackSource(ItemStack attackingItemStack);

    @Invoker("baseDamageScaleFactor")
    float quietus$baseDamageScaleFactor();

    @Invoker("doSweepAttack")
    void quietus$doSweepAttack(
            Entity excludedEntity,
            float baseDamage,
            DamageSource damageSource,
            float attackStrengthScale,
            AABB sweepHitBox
    );
}
