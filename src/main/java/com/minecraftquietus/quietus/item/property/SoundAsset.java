package com.minecraftquietus.quietus.item.property;

import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

public record SoundAsset(
    SoundEvent soundEvent,
    SoundSource soundSource
) {

    public static SoundAsset.Builder builder() {
        return new SoundAsset.Builder();
    }
    

    public static class Builder{
        private SoundEvent soundEvent;
        private SoundSource soundSource;

        public SoundAsset.Builder event(SoundEvent soundEvent) {
            this.soundEvent = soundEvent;
            return this;
        }
        public SoundAsset.Builder source(SoundSource soundSource) {
            this.soundSource = soundSource;
            return this;
        }

        public SoundAsset build() {
            return new SoundAsset(this.soundEvent, this.soundSource);
        }
    }
}
