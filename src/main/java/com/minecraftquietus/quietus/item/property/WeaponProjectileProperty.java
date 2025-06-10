package com.minecraftquietus.quietus.item.property;

import java.util.function.Function;

import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectile;

import net.minecraft.world.entity.EntityType;


public record WeaponProjectileProperty(
    float damage,
    double critChance,
    Function<Float,Float> critOperation,
    float knockback,
    float gravity,
    int persistanceTicks,
    EntityType<? extends QuietusProjectile> projectileType,
    boolean isCustom // whether projectileType uses base class of QuietusProjectile or its successor. If false, NonAmmoProjectileWeaponItem will implement only the projectileType
) {
    
    public static class Builder {
        private float damage;
        private double critChance;
        private Function<Float,Float> critOperation;
        private float knockback;
        private float gravity;
        private int persistanceTicks;
        private EntityType<? extends QuietusProjectile> projectileType;
        private boolean isCustom;

        
        public WeaponProjectileProperty.Builder damage(float value) {
            this.damage = value;
            return this;
        }
        public WeaponProjectileProperty.Builder critChance(double value) {
            this.critChance = value;
            return this;
        }
        public WeaponProjectileProperty.Builder critOperation(Function<Float,Float> func) {
            this.critOperation = func;
            return this;
        }
        public WeaponProjectileProperty.Builder knockback(float value) {
            this.knockback = value;
            return this;
        }
        public WeaponProjectileProperty.Builder gravity(float value) {
            this.gravity = value;
            return this;
        }
        public WeaponProjectileProperty.Builder persistanceTicks(int value) {
            this.persistanceTicks = value;
            return this;
        }
        public WeaponProjectileProperty.Builder projectileType(EntityType projectileType) {
            this.projectileType = projectileType;
            this.isCustom = projectileType.getBaseClass().isAssignableFrom(QuietusProjectile.class);
            return this;
        }

        public WeaponProjectileProperty build() {
            return new WeaponProjectileProperty(this.damage, this.critChance, this.critOperation, this.knockback, this.gravity, this.persistanceTicks, this.projectileType, this.isCustom);
        }
    }

}
