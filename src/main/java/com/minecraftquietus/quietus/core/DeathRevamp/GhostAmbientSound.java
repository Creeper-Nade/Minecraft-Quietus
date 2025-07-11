package com.minecraftquietus.quietus.core.DeathRevamp;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GhostAmbientSound extends AbstractTickableSoundInstance {
    private static final float FADE_SPEED = 0.02f;
    private float targetVolume;

    public GhostAmbientSound(SoundEvent sound) {
        super(sound, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.volume = 0.0f; // Start silent
        this.delay = 0;
        this.relative = true;
        this.targetVolume=0.7f;
    }

    @Override
    public void tick() {
        // Fade in
        if (volume < targetVolume) {
            volume = Math.min(volume + FADE_SPEED, targetVolume);
        }
        // Fade out
        else if (volume > targetVolume) {
            volume = Math.max(volume - FADE_SPEED, targetVolume);
            if (volume <= 0.0f) {
                stop();
            }
        }
    }

    public void fadeOut() {
        targetVolume = 0.0f;

    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
