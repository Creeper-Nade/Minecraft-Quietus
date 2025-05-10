package com.minecraftquietus.quietus.util.mana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManaComponent {
    public static int mana=0;
    public static int maxMana=20;
    
    private long lastRegenTime;
    //private final int[] slotAnimOffsets = new int[40];

    private long globalBlinkEndTime; // Tick time when global blink should end
    /*
    // Constructor for Codec
    public ManaComponent(int currentMana, int maxMana) {
        mana = Math.min(mana, maxMana);
        this.maxMana = maxMana;
    }

    // Default constructor
    public ManaComponent() {
        this(40, 40);
        for(int i = 0; i < slotAnimOffsets.length; i++) {
            slotAnimOffsets[i] = i * 3; // Staggered animation
        }
    } */


    public void tick(Player player) {
        if (player.isCreative()) return;
        if (!isFull() && player.tickCount - lastRegenTime >= 5) {
            setMana(mana + 1);
            lastRegenTime = player.tickCount;
        }
    }

    // Getters and setters
    public int getMana() { return mana; }


    public void setMana(int value) {
        int prev = mana;
        mana = Mth.clamp(value, 0, maxMana);

        // Trigger global blink when completing ANY slot
        if ((prev / 4) < (mana / 4)) {
            globalBlinkEndTime = Minecraft.getInstance().player.tickCount + 2; // 0.2s blink
        }
    }

    public boolean shouldBlinkContainers( int currentTick) {
        return currentTick < globalBlinkEndTime;
    }
    public int getTotalSlots() {
        return (int) Math.ceil(maxMana / 4.0);
    }

    public int getRowCount() {
        return (int) Math.ceil((double) getTotalSlots() / 10);
    }

    public boolean isFull() {
        return mana >= maxMana;
    }
    /*
    // Codec for serialization
    public static final Codec<ManaComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("mana").forGetter(ManaComponent::getMana),
                    Codec.INT.fieldOf("max_mana").forGetter(ManaComponent::getMaxMana)
            ).apply(instance, ManaComponent::new)
    );

     */

    // Attachment registration
    public static final AttachmentType<ManaComponent> ATTACHMENT =
            AttachmentType.builder(ManaComponent::new)
                    .build();




}

