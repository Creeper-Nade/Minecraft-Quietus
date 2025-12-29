package com.minecraftquietus.quietus.item.property;


public record GrapplingHookProperty(
        float maxRange,
        float pullStrength,
        float frictionMultiplier
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float maxRange = 100.0F;
        private float pullStrength = 0.1F;
        private float frictionMultiplier = 0.99F;

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

        public GrapplingHookProperty build() {
            return new GrapplingHookProperty(maxRange, pullStrength, frictionMultiplier);
        }
    }
}