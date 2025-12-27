package com.minecraftquietus.quietus.item.property;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import java.util.function.BiFunction;

public record GrapplingHookProperty(
        float maxRange,
        double speed,
        float pullStrength,
        float frictionMultiplier,
        EntityType<? extends Projectile> projectileType,
        BiFunction<Level, Player, ? extends Projectile> projectileFactory
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float maxRange = 100.0F;
        private double speed = 5.0D;
        private float pullStrength = 0.1F;
        private float frictionMultiplier = 0.99F;
        private EntityType<? extends Projectile> projectileType;
        private BiFunction<Level, Player, ? extends Projectile> projectileFactory;

        public Builder maxRange(float maxRange) {
            this.maxRange = maxRange;
            return this;
        }

        public Builder speed(double speed) {
            this.speed = speed;
            return this;
        }

        public Builder pullStrength(float pullStrength) {
            this.pullStrength = pullStrength;
            return this;
        }

        public Builder frictionMultiplier(float frictionMultiplier) {
            this.frictionMultiplier = frictionMultiplier;
            return this;
        }

        public Builder projectileType(EntityType<? extends Projectile> projectileType) {
            this.projectileType = projectileType;
            return this;
        }

        public Builder projectileFactory(BiFunction<Level, Player, ? extends Projectile> projectileFactory) {
            this.projectileFactory = projectileFactory;
            return this;
        }

        public GrapplingHookProperty build() {
            return new GrapplingHookProperty(maxRange, speed, pullStrength, frictionMultiplier,
                    projectileType, projectileFactory);
        }
    }
}