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

public record ManaOperating(
    int amount,
    Operation operation,
    int minAmount
) {
    // Serialization Codec
    public static final Codec<ManaOperating> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("amount").forGetter(ManaOperating::amount),
            ManaOperating.Operation.CODEC.optionalFieldOf("operation", Operation.ADDITION).forGetter(ManaOperating::operation), 
            Codec.INT.optionalFieldOf("minimum_amount", 0).forGetter(ManaOperating::minAmount)
        ).apply(instance, ManaOperating::new)
    );
    // Serialization Codec for network
    public static final StreamCodec<ByteBuf, ManaOperating> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, ManaOperating::amount,
        Operation.STREAM_CODEC, ManaOperating::operation,
        ByteBufCodecs.INT, ManaOperating::minAmount,
        ManaOperating::new
    );

    public int calculate(int mana, int maxMana) {
        return this.operation.apply(mana, maxMana, this.amount, this.minAmount);
    }

    public static class Builder {
        private int amount;
        private Operation operation = Operation.ADDITION; // default addition
        private int minAmount = 0; // deafult 0

        public ManaOperating.Builder amount(int amount) {
            this.amount = amount;
            return this;
        }
        public ManaOperating.Builder operation(Operation operation) {
            this.operation = operation;
            return this;
        }
        public ManaOperating.Builder minAmount(int minAmount) {
            this.minAmount = minAmount;
            return this;
        }

        public ManaOperating build() {
            return new ManaOperating(this.amount, this.operation, this.minAmount);
        }
    }


    public enum Operation implements StringRepresentable {
        
        ADDITION("addition", (byte)0, 
            (mana, maxMana, amount, minAmount) -> Math.max(minAmount, mana + amount)
        ),
        PERCENTAGE_MULT("percentage_multiply", (byte)1, 
            (mana, maxMana, amount, minAmount) -> Math.max(minAmount, (int)Math.round(mana * amount/100.0d))
        ),
        PERCENTAGE_MULT_OF_MAX("percentage_multiply_by_max", (byte)2, 
            (mana, maxMana, amount, minAmount) -> Math.max(minAmount, (int)Math.round(maxMana * amount/100.0d))
        ),
        SET_TO("set_to", (byte)3, 
            (mana, maxMana, amount, minAmount) -> Math.max(minAmount, amount)
        );

        public static final IntFunction<ManaOperating.Operation> BY_ID = ByIdMap.continuous(
            ManaOperating.Operation::getIndex, values(), ByIdMap.OutOfBoundsStrategy.ZERO
        );
        public static final StreamCodec<ByteBuf, ManaOperating.Operation> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ManaOperating.Operation::getIndex);
        public static final Codec<ManaOperating.Operation> CODEC = StringRepresentable.fromEnum(ManaOperating.Operation::values);

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
