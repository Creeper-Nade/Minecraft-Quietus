package com.minecraftquietus.quietus.enchantment.effect_components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record crit_chance(int value) {
    public static final Codec<crit_chance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("value").forGetter(crit_chance::value)
            ).apply(instance, crit_chance::new)
    );

    public int add(int x) {
        return value() + x;
    }
}
