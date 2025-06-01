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

public record UsesMana(
    int amount,
    Operation operation,
    int minAmount
) {
    // Serialization Codec
    public static final Codec<UsesMana> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("amount").forGetter(UsesMana::amount),
            UsesMana.Operation.CODEC.optionalFieldOf("operation", Operation.ADDITION).forGetter(UsesMana::operation), 
            Codec.INT.optionalFieldOf("minimum_amount", 0).forGetter(UsesMana::minAmount)
        ).apply(instance, UsesMana::new)
    );
    // Serialization Codec for network
    public static final StreamCodec<ByteBuf, UsesMana> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, UsesMana::amount,
        Operation.STREAM_CODEC, UsesMana::operation,
        ByteBufCodecs.INT, UsesMana::minAmount,
        UsesMana::new
    );

    public int calculateConsumption(int mana, int maxMana) {
        return this.operation.apply(mana, maxMana, this.amount, this.minAmount);
    }

    public static class Builder {
        private int amount;
        private Operation operation = Operation.ADDITION; // default addition
        private int minAmount = 0; // deafult 0

        public UsesMana.Builder amount(int amount) {
            this.amount = amount;
            return this;
        }
        public UsesMana.Builder operation(Operation operation) {
            this.operation = operation;
            return this;
        }
        public UsesMana.Builder minAmount(int minAmount) {
            this.minAmount = minAmount;
            return this;
        }

        public UsesMana build() {
            return new UsesMana(this.amount, this.operation, this.minAmount);
        }
    }


    public enum Operation implements StringRepresentable {
        
        ADDITION("addition", (byte)0, 
            (mana, maxMana, amount, minAmount) -> Math.max(minAmount, amount)
        ),
        PERCENTAGE_MULT("percentage_multiply", (byte)1, 
            (mana, maxMana, amount, minAmount) -> Math.max(minAmount, (int)Math.round(mana * ((double)amount/100.0d)))
        ),
        PERCENTAGE_MULT_OF_MAX("percentage_multiply_by_max", (byte)2, 
            (mana, maxMana, amount, minAmount) -> Math.max(minAmount, (int)Math.round(maxMana * ((double)amount/100.0d)))
        ),
        SET_TO("set_to", (byte)3, 
            (mana, maxMana, amount, minAmount) -> Math.max(minAmount, mana - amount)
        );

        public static final IntFunction<UsesMana.Operation> BY_ID = ByIdMap.continuous(
            UsesMana.Operation::getIndex, values(), ByIdMap.OutOfBoundsStrategy.ZERO
        );
        public static final StreamCodec<ByteBuf, UsesMana.Operation> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, UsesMana.Operation::getIndex);
        public static final Codec<UsesMana.Operation> CODEC = StringRepresentable.fromEnum(UsesMana.Operation::values);

        private String name;
        private byte index;
        private Operator<Integer,Integer,Integer,Integer,Integer> func;

        Operation(String name, byte index, Operator<Integer,Integer,Integer,Integer, Integer> func) {
            this.name = name;
            this.index = index;
            this.func = func;
        }
        public byte getIndex() {
            return this.index;
        }
        @Override
        public String getSerializedName() {
            return this.name;
        }
        public int apply(int mana, int maxMana, int amount, int minAmount) {
            return this.func.apply(mana, maxMana, amount, minAmount);
        }
    }

    @FunctionalInterface
    public interface Operator<A,B,C,D,R> {
        R apply(A a,B b,C c,D d);
    }
}
