package com.minecraftquietus.quietus.item.component;

import java.util.Optional;

import com.minecraftquietus.quietus.item.QuietusComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record CanDecay(
    int maxDecay,
    ItemStack convertInto
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
        if (this.setDecay(itemstack, itemstack.getOrDefault(QuietusComponents.DECAY.get(), 0).intValue()+amount)) {
            ItemStack newstack = this.convertInto.copy();
            newstack.setCount(itemstack.getCount());
            return Optional.of(newstack);
        }
        return Optional.empty();
    }

    // Serialization Codec
    public static final Codec<CanDecay> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("max_decay").forGetter(CanDecay::maxDecay),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("convert_into", ItemStack.EMPTY).forGetter(CanDecay::convertInto)
        ).apply(instance, CanDecay::new)
    );
    // Serialization Codec for network
    public static final StreamCodec<RegistryFriendlyByteBuf, CanDecay> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, CanDecay::maxDecay,
        ItemStack.OPTIONAL_STREAM_CODEC, CanDecay::convertInto,
        CanDecay::new
    );

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            return other instanceof CanDecay candecay 
                ? this.convertInto.getItem().equals(candecay.convertInto().getItem()) && this.convertInto.getCount() == candecay.convertInto().getCount() && this.convertInto.getComponentsPatch().equals(candecay.convertInto().getComponentsPatch()) 
                    && this.maxDecay == candecay.maxDecay()
                : false;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxDecay;
        private ItemStack convertsInto = ItemStack.EMPTY; // default convert item

        public CanDecay.Builder maxDecay(int maxDecay) {
            this.maxDecay = maxDecay;
            return this;
        }
        public CanDecay.Builder convertsInto(ItemStack convertsInto) {
            this.convertsInto = convertsInto;
            return this;
        }

        public CanDecay build() {
            return new CanDecay(this.maxDecay, this.convertsInto);
        }
    }
}
