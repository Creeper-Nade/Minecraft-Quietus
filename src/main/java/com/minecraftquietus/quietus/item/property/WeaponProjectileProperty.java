package com.minecraftquietus.quietus.item.property;

public record WeaponProjectileProperty(
    float damage,
    double critChance,
    float knockback,
    float gravity,
    int persistanceTicks
) {
    
    public static class Builder {
        private float damage;
        private double critChance;
        private float knockback;
        private float gravity;
        private int persistanceTicks;

        
        public WeaponProjectileProperty.Builder damage(float value) {
            this.damage = value;
            return this;
        }
        public WeaponProjectileProperty.Builder critChance(double value) {
            this.critChance = value;
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
            this.critChance = value;
            return this;
        }

        public WeaponProjectileProperty build() {
            return new WeaponProjectileProperty(this.damage, this.critChance, this.knockback, this.gravity, this.persistanceTicks);
        }
    }

}
