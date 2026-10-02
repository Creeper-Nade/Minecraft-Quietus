package com.quietus.item.property;

import java.util.function.Function;

import com.quietus.entity.projectiles.QuietusProjectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;


public record QuietusProjectileProperty(
    float damage,
    double critChance,
    Function<Float,Float> critOperation,
    float knockback,
    float velocityMult,
    int persistanceTicks,
    EntityType<? extends Projectile> projectileType,
    boolean isCustom // whether projectileType uses base class of QuietusProjectile or its children classes. If false, QuietusProjectileWeaponItem will not configure its shot projectiles as provided
) {
    
    public QuietusProjectileProperty(
        float damage,
        double critChance,
        Function<Float,Float> critOperation,
        float knockback,
        int persistanceTicks,
        EntityType<? extends QuietusProjectile> projectileType,
        boolean isCustom
    ) {
        this(damage, critChance, critOperation, knockback, 1.0f, persistanceTicks, projectileType, isCustom);
    }

    public static QuietusProjectileProperty.Builder builder() {
        return new QuietusProjectileProperty.Builder();
    }

    public static class Builder {
        private float damage;
        private double critChance;
        private Function<Float,Float> critOperation;
        private float knockback;
        private float velocityMult = 1.0f;
        private int persistanceTicks;
        private EntityType<? extends Projectile> projectileType;
        private boolean isCustom;

        
        public QuietusProjectileProperty.Builder damage(float value) {
            this.damage = value;
            return this;
        }
        public QuietusProjectileProperty.Builder critChance(double value) {
            this.critChance = value;
            return this;
        }
        public QuietusProjectileProperty.Builder critOperation(Function<Float,Float> func) {
            this.critOperation = func;
            return this;
        }
        public QuietusProjectileProperty.Builder knockback(float value) {
            this.knockback = value;
            return this;
        }
        public QuietusProjectileProperty.Builder velocityMult(float value) {
            this.velocityMult = value;
            return this;
        }
        public QuietusProjectileProperty.Builder persistanceTicks(int value) {
            this.persistanceTicks = value;
            return this;
        }
        public QuietusProjectileProperty.Builder projectileType(EntityType projectileType) {
            this.projectileType = projectileType;
            this.isCustom = projectileType.getBaseClass().isAssignableFrom(QuietusProjectile.class);
            return this;
        }

        public QuietusProjectileProperty build() {
            return new QuietusProjectileProperty(this.damage, this.critChance, this.critOperation, this.knockback, this.velocityMult, this.persistanceTicks, this.projectileType, this.isCustom);
        }
    }

}
