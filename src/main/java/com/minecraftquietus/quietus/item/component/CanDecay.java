package com.minecraftquietus.quietus.item.component;

import java.util.Optional;

import com.minecraftquietus.quietus.item.QuietusComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record CanDecay(
    int maxDecay,
    Holder<Item> convertInto
) {
    public float getDecayFraction(int decay) {
        return 1.0f - (float)decay / (float)this.maxDecay;
    }
    public int getDisplayColor(int decay) {
        return this.getDisplayColor(decay, 0xFF);
    }
    public int getDisplayColor(int decay, int transparency) {
        int pristine = 0xFF289B00;  // opaque green
        int fresh = 0xFFAABB00;  // opaque lime 
        int stale = 0xFFFFA500;  // opaque orange
        int spoiled = 0xFFEE2222;  // opaque red
        int defaultColor = 0xFFFFFFFF; // opaque white

        float fraction = this.getDecayFraction(decay);
        if (fraction >= 0.8f && fraction <= 1.0f) {
            return (pristine & 0x00FFFFFF) | ((transparency & 0xFF) << 24);
        } else if (fraction >= 0.5f) {
            return (fresh & 0x00FFFFFF) | ((transparency & 0xFF) << 24);
        } else if (fraction >= 0.2f) {
            return (stale & 0x00FFFFFF) | ((transparency & 0xFF) << 24);
        } else if (fraction >= 0.0f) {
            return (spoiled & 0x00FFFFFF) | ((transparency & 0xFF) << 24);
        } else {
            return (defaultColor & 0x00FFFFFF) | ((transparency & 0xFF) << 24);
        }
    }
    /**
     * Sets the decay value for item stack, while checking own maxDecay.
     * Note: this sets the decay bounded within 0 to own maxDecay.
     * @param itemstack the item stack checked for
     * @param value decay value to set to
     * @return if value greater than maxDecay, returns true, else returns false
     */
    public boolean setDecay(ItemStack itemstack, int value) {
        itemstack.set(QuietusComponents.DECAY.get(), Mth.clamp(value, 0, this.maxDecay));
        return value > this.maxDecay;
    }
    public Optional<ItemStack> changeDecayAndMakeConvertedItemIfDecayed(ItemStack itemstack, int amount) {
        if (this.setDecay(itemstack, itemstack.getOrDefault(QuietusComponents.DECAY.get(), 0).intValue() + amount)) {
            ItemStack newstack = new ItemStack(this.convertInto); 
            newstack.setCount(itemstack.getCount());
            return Optional.of(newstack);
        }
        return Optional.empty();
    }

    // Serialization Codec
    public static final Codec<CanDecay> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("max_decay").forGetter(CanDecay::maxDecay),
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("convert_into").forGetter(CanDecay::convertInto)
        ).apply(instance, CanDecay::new)
    );
    // Serialization Codec for network
    public static final StreamCodec<RegistryFriendlyByteBuf, CanDecay> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, CanDecay::maxDecay,
        ByteBufCodecs.holderRegistry(Registries.ITEM), CanDecay::convertInto,
        CanDecay::new
    );

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            return other instanceof CanDecay candecay 
                ? this.convertInto.equals(candecay.convertInto())
                    && this.maxDecay == candecay.maxDecay()
                : false;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxDecay;
        private Holder<Item> convertsInto; // default convert item

        public CanDecay.Builder maxDecay(int maxDecay) {
            this.maxDecay = maxDecay;
            return this;
        }
        public CanDecay.Builder convertsInto(Holder<Item> convertsInto) {
            this.convertsInto = convertsInto;
            return this;
        }

        public CanDecay build() {
            return new CanDecay(this.maxDecay, this.convertsInto);
        }
    }
}
