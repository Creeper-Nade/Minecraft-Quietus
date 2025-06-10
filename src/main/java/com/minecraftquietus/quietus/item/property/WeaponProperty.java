package com.minecraftquietus.quietus.item.property;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.minecraftquietus.quietus.util.TriFunction;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public record WeaponProperty(
    int projectilesPerShot, // how many projectiles per use
    TriFunction<Float,Integer,RandomSource,Float> xRotOffsetCalc, // function for calculating x rotation offsets when shooting
    TriFunction<Float,Integer,RandomSource,Float> yRotOffsetCalc, // function for calculating y rotation offsets when shooting
    float shootVelocity, // function for calculating velocity multiplier when shooting
    Predicate<ItemStack> supportedProjectiles, // supported projectile items by this weapon
    int attackRange // attack range of this weapon. Used for AI to determine range
) {
    
    public static class Builder {
        int projectilesPerShot;
        TriFunction<Float,Integer,RandomSource,Float> xRotOffsetCalc;
        TriFunction<Float,Integer,RandomSource,Float> yRotOffsetCalc;
        float shootVelocity;
        Predicate<ItemStack> supportedProjectiles;
        int attackRange;

        public WeaponProperty.Builder projectilesPerShot(int value) {
            this.projectilesPerShot = value;
            return this;
        }
        public WeaponProperty.Builder xRotOffsetFunc(TriFunction<Float,Integer,RandomSource,Float> func) {
            this.xRotOffsetCalc = func;
            return this;
        }
        public WeaponProperty.Builder yRotOffsetFunc(TriFunction<Float,Integer,RandomSource,Float> func) {
            this.yRotOffsetCalc = func;
            return this;
        }
        public WeaponProperty.Builder shootVelocity(float value) {
            this.shootVelocity = value;
            return this;
        }
        public WeaponProperty.Builder supportedProjectiles(Predicate<ItemStack> func) {
            this.supportedProjectiles = func;
            return this;
        }
        public WeaponProperty.Builder attackRange(int value) {
            this.attackRange = value;
            return this;
        }

        public WeaponProperty build() {
            return new WeaponProperty(this.projectilesPerShot, this.xRotOffsetCalc, this.yRotOffsetCalc, this.shootVelocity, this.supportedProjectiles, this.attackRange);
        }
    }
}
