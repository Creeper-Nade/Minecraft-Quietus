package com.minecraftquietus.quietus.util.mana;

import com.minecraftquietus.quietus.util.PlayerData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManaComponent  {
    public static int mana=0;
    public static int maxMana=20;
    public static int RegenCDTick=5;
    
    private long lastRegenTime;
    //private final int[] slotAnimOffsets = new int[40];

    private long globalBlinkEndTime; // Tick time when global blink should end
    private int slots;
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
        if (!isFull() && player.tickCount - lastRegenTime >= RegenCDTick) {
            setMana(mana + 1,player);
            lastRegenTime = player.tickCount;
        }
        //System.out.println("global"+globalBlinkEndTime);
    }

    // Getters and setters
    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }


    public void setMana(int value, Player player) {
        int prev = mana;
        mana = Mth.clamp(value, 0, maxMana);

        if (player instanceof ServerPlayer serverPlayer) {


            PlayerData.ManapackToPlayer(serverPlayer,this);
        }

        // Trigger global blink when completing ANY slot
        if ((prev / 4) < (mana / 4)) {
            globalBlinkEndTime = player.tickCount + 2; // 0.2s blink

        }
        //System.out.println("global"+globalBlinkEndTime);
    }

    public boolean shouldBlinkContainers( int currentTick) {
        //System.out.println("global"+globalBlinkEndTime);

        return currentTick < globalBlinkEndTime;
    }
    public int getTotalSlots(int MaxMana) {
        slots=(int) Math.ceil(MaxMana / 4.0);
        return slots;
    }

    public int getRowCount() {

        return (int) Math.ceil((double)slots / 10);
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

