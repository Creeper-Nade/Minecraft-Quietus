package com.quietus.skilltree;

import java.util.Optional;

import com.quietus.core.QuietusRegistries;
import com.quietus.util.SkillUtil;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public record Reward(
    Identifier skillLocation,
    Number amount,
    Optional<String> source
) {
    public static final Codec<Number> NUMBER_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<Number, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<String> strResult = ops.getStringValue(input);
            if (strResult.isSuccess()) {
                String str = strResult.getOrThrow().trim();
                try {
                    if (str.endsWith("f") || str.endsWith("F")) {
                        float f = Float.parseFloat(str.substring(0, str.length() - 1));
                        return DataResult.success(Pair.of(f, ops.empty()));
                    } else if (str.endsWith("d") || str.endsWith("D")) {
                        double d = Double.parseDouble(str.substring(0, str.length() - 1));
                        return DataResult.success(Pair.of(d, ops.empty()));
                    } else if (str.contains(".")) {
                        double d = Double.parseDouble(str);
                        return DataResult.success(Pair.of(d, ops.empty()));
                    } else {
                        int i = Integer.parseInt(str);
                        return DataResult.success(Pair.of(i, ops.empty()));
                    }
                } catch (NumberFormatException e) {
                    return DataResult.error(() -> "Invalid number format: " + str);
                }
            }

            DataResult<Number> numResult = ops.getNumberValue(input);
            if (numResult.isSuccess()) {
                Number num = numResult.getOrThrow();
                if (num instanceof Float) {
                    return DataResult.success(Pair.of(num, ops.empty()));
                } else if (num instanceof Double) {
                    return DataResult.success(Pair.of(num, ops.empty()));
                } else if (num instanceof Integer || num instanceof Byte || num instanceof Short) {
                    return DataResult.success(Pair.of(num.intValue(), ops.empty()));
                }

                String str = num.toString().trim();
                try {
                    if (str.endsWith("f") || str.endsWith("F")) {
                        float f = Float.parseFloat(str.substring(0, str.length() - 1));
                        return DataResult.success(Pair.of(f, ops.empty()));
                    } else if (str.endsWith("d") || str.endsWith("D")) {
                        double d = Double.parseDouble(str.substring(0, str.length() - 1));
                        return DataResult.success(Pair.of(d, ops.empty()));
                    } else if (str.contains(".")) {
                        double d = Double.parseDouble(str);
                        return DataResult.success(Pair.of(d, ops.empty()));
                    } else {
                        int i = Integer.parseInt(str);
                        return DataResult.success(Pair.of(i, ops.empty()));
                    }
                } catch (NumberFormatException e) {
                    return DataResult.error(() -> "Invalid number format: " + str);
                }
            }

            return DataResult.error(() -> "Expected a number or number string, got " + input);
        }

        @Override
        public <T> DataResult<T> encode(Number input, DynamicOps<T> ops, T prefix) {
            if (input instanceof Float) {
                return DataResult.success(ops.createString(input.floatValue() + "f"));
            } else if (input instanceof Double) {
                return DataResult.success(ops.createDouble(input.doubleValue()));
            } else {
                return DataResult.success(ops.createInt(input.intValue()));
            }
        }
    };

    public static final StreamCodec<FriendlyByteBuf, Number> NUMBER_STREAM_CODEC = StreamCodec.of(
        (buffer, value) -> {
            if (value instanceof Float) {
                buffer.writeByte(1);
                buffer.writeFloat(value.floatValue());
            } else if (value instanceof Double) {
                buffer.writeByte(2);
                buffer.writeDouble(value.doubleValue());
            } else {
                buffer.writeByte(0);
                buffer.writeInt(value.intValue());
            }
        },
        buffer -> {
            byte type = buffer.readByte();
            return switch (type) {
                case 1 -> buffer.readFloat();
                case 2 -> buffer.readDouble();
                default -> buffer.readInt();
            };
        }
    );

    public static final Codec<Reward> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Identifier.CODEC.fieldOf("skill").forGetter(Reward::skillLocation),
            NUMBER_CODEC.optionalFieldOf("amount", 0).forGetter(Reward::amount),
            Codec.STRING.optionalFieldOf("source").forGetter(Reward::source)
        ).apply(instance, Reward::new)
    );

    public static final StreamCodec<FriendlyByteBuf, Reward> STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, Reward::skillLocation,    
        NUMBER_STREAM_CODEC, Reward::amount,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional), Reward::source,
        Reward::new
    );

    public void apply(Player player) {
        this.apply(player, "none");
    }

    public void apply(Player player, String defaultSource) {
        if (QuietusRegistries.SKILL_REGISTRY.containsKey(this.skillLocation)) {
            String effectiveSource = this.source.orElse(defaultSource);
            SkillUtil.addSkillLevel(player, QuietusRegistries.SKILL_REGISTRY.getValue(this.skillLocation), this.amount, effectiveSource);
        }
    }

    public void apply(Player player, Identifier defaultSource) {
        this.apply(player, defaultSource != null ? defaultSource.toString() : "none");
    }

    public static Reward make(Identifier skillLocation, Number amount, Optional<String> source) {
        return new Reward(skillLocation, amount, source);
    }

    public static Reward make(Identifier skillLocation, Number amount, String source) {
        return new Reward(skillLocation, amount, Optional.ofNullable(source));
    }

    public static Reward make(Identifier skillLocation, Number amount) {
        return new Reward(skillLocation, amount, Optional.empty());
    }

    public Number percentageAmount() {
        if (this.amount instanceof Float) {
            return this.amount.floatValue() * 100.0f;
        } else if (this.amount instanceof Double) {
            return this.amount.doubleValue() * 100.0d;
        } else {
            return this.amount.intValue() * 100;
        }
    }

    @Override
    public String toString() {
        return CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, this)
            .result()
            .map(com.google.gson.JsonElement::toString)
            .orElse("{}");
    }
}

