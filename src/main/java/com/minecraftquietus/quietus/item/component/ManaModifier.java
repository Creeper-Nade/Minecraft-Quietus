package com.minecraftquietus.quietus.item.component;

import java.util.function.IntFunction;

import org.checkerframework.checker.units.qual.min;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record ManaModifier(
    int amount,
    Operation operation,
    int minAmount
) {
    // Serialization Codecgo
    public static final Codec<ManaModifier> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("amount").forGetter(ManaModifier::amount),
            ManaModifier.Operation.CODEC.optionalFieldOf("operation", Operation.ADDITION).forGetter(ManaModifier::operation), 
            Codec.INT.optionalFieldOf("minimum_amount", 0).forGetter(ManaModifier::minAmount)
        ).apply(instance, ManaModifier::new)
    );
    // Serialization Codec for network
    public static final StreamCodec<ByteBuf, ManaModifier> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, ManaModifier::amount,
        Operation.STREAM_CODEC, ManaModifier::operation,
        ByteBufCodecs.INT, ManaModifier::minAmount,
        ManaModifier::new
    );

    public static class Builder {
        private int amount;
        private Operation operation = Operation.ADDITION; // default addition
        private int minAmount = 0; // deafult 0

        public ManaModifier.Builder amount(int amount) {
            this.amount = amount;
            return this;
        }
        public ManaModifier.Builder operation(Operation operation) {
            this.operation = operation;
            return this;
        }
        public ManaModifier.Builder minAmount(int minAmount) {
            this.minAmount = minAmount;
            return this;
        }

        public ManaModifier build() {
            return new ManaModifier(this.amount, this.operation, this.minAmount);
        }
    }


    public enum Operation implements StringRepresentable {
        ADDITION("addition", (byte)0),
        PERCENTAGE_MULT("percentage_multiply", (byte)1),
        PERCENTAGE_MULT_OF_MAX("percentage_multiply_by_max", (byte)2),
        SET_TO("set_to", (byte)3);

        public static final IntFunction<ManaModifier.Operation> BY_ID = ByIdMap.continuous(
            ManaModifier.Operation::getIndex, values(), ByIdMap.OutOfBoundsStrategy.ZERO
        );
        public static final StreamCodec<ByteBuf, ManaModifier.Operation> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ManaModifier.Operation::getIndex);
        public static final Codec<ManaModifier.Operation> CODEC = StringRepresentable.fromEnum(ManaModifier.Operation::values);

        private String name;
        private byte index;

        Operation(String name, byte index) {
            this.name = name;
            this.index = index;
        }
        public byte getIndex() {
            return this.index;
        }
        @Override
        public String getSerializedName() {
            return this.name;
        }

    }
}
