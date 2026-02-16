package com.minecraftquietus.quietus.item.property;


public record GrapplingHookProperty(
        float maxRange,
        float pullStrength,
        float frictionMultiplier,
        float maxPullSpeed
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float maxRange = 10.0F;
        private float pullStrength = 1.5F;
        private float frictionMultiplier = 0.99F;
        private float maxPullSpeed = 99.0F; // default max pull speed

        public Builder maxRange(float maxRange) {
            this.maxRange = maxRange;
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
        public Builder maxPullSpeed(float maxPullSpeed) { this.maxPullSpeed = maxPullSpeed; return this; }

        public GrapplingHookProperty build() {
            return new GrapplingHookProperty(maxRange, pullStrength, frictionMultiplier,maxPullSpeed);
        }
    }
}