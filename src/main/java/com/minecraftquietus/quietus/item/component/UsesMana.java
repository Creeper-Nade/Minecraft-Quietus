package com.minecraftquietus.quietus.item.component;

import java.util.function.IntFunction;

import com.minecraftquietus.quietus.enchantment.QuietusEnchantmentHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record UsesMana(
    int amount,
    Operation operation,
    int minAmount
) {
    // Serialization Codec
    public static final Codec<UsesMana> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("amount").forGetter(UsesMana::amount),
            UsesMana.Operation.CODEC.optionalFieldOf("operation", Operation.ADD_VALUE).forGetter(UsesMana::operation), 
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

    public int calculateConsumption(int mana, int maxMana, ItemStack item, Level level) {
        //for the "Conservation" enchantment, which reduces mana cost.
        //the ItemStack and the level of the owner has to be passed in here for enchantmentHelper
        int RealAmount= this.amount();
        if(level instanceof ServerLevel serverLevel) {
            float cost_reduction=QuietusEnchantmentHelper.modifyManaCost(serverLevel, item, 0.0f);
            RealAmount = Math.round(this.amount()*(1-cost_reduction));
        }
        return this.operation.apply(mana, maxMana, RealAmount, this.minAmount());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            return other instanceof UsesMana usesmana 
                ? this.amount == usesmana.amount() && this.operation.equals(usesmana.operation()) && this.minAmount == usesmana.minAmount()
                : false;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int amount;
        private Operation operation = Operation.ADD_VALUE; // default addition
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
        
        ADD_VALUE("add_value", (byte)0, 
            (mana, maxMana, amount, minAmount) -> Math.max(minAmount, amount)
        ),
        ADD_MULTIPLIED_CURRENT("add_multiplied_current", (byte)1, 
            (mana, maxMana, amount, minAmount) -> Math.max(minAmount, (int)Math.round(mana * ((double)amount/100.0d)))
        ),
        ADD_MULTIPLIED_TOTAL("add_multiplied_total", (byte)2, 
            (mana, maxMana, amount, minAmount) -> Math.max(minAmount, (int)Math.round(maxMana * ((double)amount/100.0d)))
        ),
        SET_VALUE("set_value", (byte)3, 
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
