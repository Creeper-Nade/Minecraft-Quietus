package com.minecraftquietus.quietus.item.property;

import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

public record SoundAsset(
    String name,
    SoundEvent soundEvent,
    SoundSource soundSource
) {
    

    public static class Builder{
        private String name;
        private SoundEvent soundEvent;
        private SoundSource soundSource;

        public SoundAsset.Builder name(String name) {
            this.name = name;
            return this;
        }
        public SoundAsset.Builder event(SoundEvent soundEvent) {
            this.soundEvent = soundEvent;
            return this;
        }
        public SoundAsset.Builder source(SoundSource soundSource) {
            this.soundSource = soundSource;
            return this;
        }

        public SoundAsset build() {
            return new SoundAsset(this.name, this.soundEvent, this.soundSource);
        }
    }
}
