package com.minecraftquietus.quietus.item.property;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.minecraftquietus.quietus.util.TriFunction;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public record ProjectileWeaponProperty(
    int projectilesPerShot, // how many projectiles per use
    TriFunction<Float,Integer,RandomSource,Float> xRotOffsetCalc, // function for calculating x rotation offsets when shooting
    TriFunction<Float,Integer,RandomSource,Float> yRotOffsetCalc, // function for calculating y rotation offsets when shooting
    float shootVelocity, // function for calculating velocity multiplier when shooting
    float shootInaccuracy, // function for calculating velocity multiplier when shooting
    Predicate<ItemStack> supportedProjectiles, // supported projectile items by this weapon
    int useDuration, // use duration for the weapon. -1 for infinite (set to 72000), 0 for none, >0 for fixed use duration
    int powerDuration, // use duration for full charge for weapon. If ≥ 0, guaranteed crit hit when the weapon is used for more ticks than powerDuration, else ignored
    int attackRange // attack range of this weapon. Used for AI to determine range
) {

    public static ProjectileWeaponProperty.Builder builder() {
        return new ProjectileWeaponProperty.Builder();
    }
    
    public static class Builder { // defaults
        int projectilesPerShot = 1;
        TriFunction<Float,Integer,RandomSource,Float> xRotOffsetCalc = (a,b,c)->a;
        TriFunction<Float,Integer,RandomSource,Float> yRotOffsetCalc = (a,b,c)->a;
        float shootVelocity = 1.0f;
        float shootInaccuracy = 0.0f;
        Predicate<ItemStack> supportedProjectiles = (itemstack)->true;
        int useDuration = 0;
        int powerDuration = -1;
        int attackRange = 15;

        public ProjectileWeaponProperty.Builder projectilesPerShot(int value) {
            this.projectilesPerShot = value;
            return this;
        }
        public ProjectileWeaponProperty.Builder xRotOffsetFunc(TriFunction<Float,Integer,RandomSource,Float> func) {
            this.xRotOffsetCalc = func;
            return this;
        }
        public ProjectileWeaponProperty.Builder yRotOffsetFunc(TriFunction<Float,Integer,RandomSource,Float> func) {
            this.yRotOffsetCalc = func;
            return this;
        }
        public ProjectileWeaponProperty.Builder shootVelocity(float value) {
            this.shootVelocity = value;
            return this;
        }
        public ProjectileWeaponProperty.Builder shootInaccuracy(float value) {
            this.shootInaccuracy = value;
            return this;
        }
        public ProjectileWeaponProperty.Builder supportedProjectiles(Predicate<ItemStack> func) {
            this.supportedProjectiles = func;
            return this;
        }
        public ProjectileWeaponProperty.Builder useDuration(int value) {
            this.useDuration = value;
            return this;
        }
        public ProjectileWeaponProperty.Builder powerDuration(int value) {
            this.powerDuration = value;
            return this;
        }
        public ProjectileWeaponProperty.Builder attackRange(int value) {
            this.attackRange = value;
            return this;
        }

        public ProjectileWeaponProperty build() {
            return new ProjectileWeaponProperty(this.projectilesPerShot, this.xRotOffsetCalc, this.yRotOffsetCalc, this.shootVelocity, this.shootInaccuracy, this.supportedProjectiles, this.useDuration, this.powerDuration, this.attackRange);
        }
    }
}
