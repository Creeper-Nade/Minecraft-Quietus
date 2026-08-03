package com.quietus.magic;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/** Immutable chanting rules owned by one magic weapon. */
public record MagicChantingPattern(
        int durationTicks,
        List<Float> checkpointCenters,
        float windowRadius,
        float leftClickChance,
        float positionJitter,
        float minimumGap
) {
    public MagicChantingPattern {
        if (durationTicks <= 0) throw new IllegalArgumentException("durationTicks must be positive");
        checkpointCenters = List.copyOf(checkpointCenters);
        if (checkpointCenters.isEmpty()) throw new IllegalArgumentException("checkpointCenters cannot be empty");
        if (windowRadius <= 0.0F || windowRadius >= 0.5F) {
            throw new IllegalArgumentException("windowRadius must be between 0 and 0.5");
        }
        if (leftClickChance < 0.0F || leftClickChance > 1.0F) {
            throw new IllegalArgumentException("leftClickChance must be between 0 and 1");
        }
        if (positionJitter < 0.0F || minimumGap < 0.0F) {
            throw new IllegalArgumentException("positionJitter and minimumGap cannot be negative");
        }
        float previousCenter = -1.0F;
        for (float center : checkpointCenters) {
            if (!Float.isFinite(center) || center < 0.0F || center > 1.0F) {
                throw new IllegalArgumentException("checkpoint centers must be between 0 and 1");
            }
            if (previousCenter >= 0.0F
                    && center - previousCenter < windowRadius * 2.0F + minimumGap) {
                throw new IllegalArgumentException("checkpoint centers must be ordered and cannot overlap");
            }
            previousCenter = center;
        }
    }

    public Generated generate(long seed) {
        RandomSource random = RandomSource.create(seed);
        int checkpointCount = checkpointCenters.size();
        List<Checkpoint> checkpoints = new ArrayList<>(checkpointCount);
        float previousCenter = 0.0F;

        for (int i = 0; i < checkpointCount; i++) {
            float nominalCenter = checkpointCenters.get(i);
            float randomizedCenter = nominalCenter
                    + (random.nextFloat() * 2.0F - 1.0F) * positionJitter;
            float minimumCenter = i == 0
                    ? 0.0F
                    : previousCenter + windowRadius * 2.0F + minimumGap;
            int remaining = checkpointCount - i - 1;
            float maximumCenter = 1.0F
                    - remaining * (windowRadius * 2.0F + minimumGap);
            float center = Mth.clamp(randomizedCenter, minimumCenter, maximumCenter);
            Input input = random.nextFloat() < leftClickChance ? Input.LEFT : Input.RIGHT;
            checkpoints.add(new Checkpoint(center, input));
            previousCenter = center;
        }

        return new Generated(checkpoints);
    }

    public enum Input {
        LEFT,
        RIGHT
    }

    public record Checkpoint(float center, Input input) {
    }

    public record Generated(List<Checkpoint> checkpoints) {
        public Generated {
            checkpoints = List.copyOf(checkpoints);
        }

        public int size() {
            return checkpoints.size();
        }
    }
}
