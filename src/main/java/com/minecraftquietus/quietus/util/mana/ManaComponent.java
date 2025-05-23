package com.minecraftquietus.quietus.util.mana;

import com.minecraftquietus.quietus.util.PlayerData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.neoforged.neoforge.common.util.INBTSerializable;

import static com.minecraftquietus.quietus.util.QuietusAttributes.MANA_REGEN_BONUS;
import static com.minecraftquietus.quietus.util.QuietusAttributes.MAX_MANA;
import static java.lang.Math.abs;

public class ManaComponent implements INBTSerializable<CompoundTag> {
    private int mana;
    private int maxMana=20;
    private double manaRate;
    private int Regen_bonus=5;
    
    private long lastRegenTime;
    //private final int[] slotAnimOffsets = new int[40];

    private long globalBlinkEndTime; // Tick time when global blink should end
    private int slots;
    /*
    // Constructor for Codec
    public ManaComponent(int currentMana, int maxMana) {
        mana = Math.min(mana, maxMana);
        this.maxMana = maxMana;
    }*/

    // Default constructor

    public ManaComponent() {
        //LivingEntity livingEntity = QuietusCommonEvents.QuietusServerPlayer;
        //AttributeMap attribute_map = livingEntity.getAttributes();
        //this.maxMana= (int)attributes.getValue(MAX_MANA);
        this.maxMana=0;
    }



    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("mana", mana);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.mana = nbt.getIntOr("mana",10);
    }


    public void tick(ServerPlayer player) {
        if (player.isCreative()) return;

        checkManaAttributes(player);
        if (this.isFull()) {
            this.manaRate = 0;
            setMana(maxMana,player);
        } else {
            setManaRegen(player);
            //lastRegenTime = player.tickCount;
        }   
        //System.out.println("global"+globalBlinkEndTime);
    }

    // Getters
    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }

    private void checkManaAttributes(ServerPlayer player)
    {
        AttributeMap attributeMap = player.getAttributes();
        if(maxMana != (int)attributeMap.getValue(MAX_MANA))
        {
            setMaxMana(player);
        }
        if(Regen_bonus != (int)attributeMap.getValue(MANA_REGEN_BONUS))
            setRegenCD(player);

    }
    public void setMaxMana(ServerPlayer player)
    {
        AttributeMap attributeMap = player.getAttributes();
        maxMana= (int)attributeMap.getValue(MAX_MANA);
        PlayerData.ManapackToPlayer(player,this);

    }
    public void setRegenCD(ServerPlayer player)
    {
        AttributeMap attributeMap = player.getAttributes();
        Regen_bonus= (int)attributeMap.getValue(MANA_REGEN_BONUS);

    }

    public void setManaRegen(ServerPlayer player)
    {
        manaRate += (((double) maxMana /3 +1+ stationary_bonus(player)+Regen_bonus) * ((mana/maxMana)*0.8+0.2)*1.15)*2;
        if(manaRate>=40)
        {
            int added_mana=(int)Math.floor(manaRate/40);
            manaRate-=40*(added_mana);
            setMana(mana+added_mana,player);
        }
    }

    public double stationary_bonus(ServerPlayer player)
    {
        if(abs(player.xCloak-player.xCloakO)<0.001)
        {
            //System.out.println(maxMana/3);
            return (double) maxMana /3;
        }
        //System.out.println(0);
        return 0;
    }
    public void setMana(int value, ServerPlayer player) {
        int prev = mana;
        mana = Mth.clamp(value, 0, maxMana);
        PlayerData.ManapackToPlayer(player,this);
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
        return this.mana >= this.maxMana;
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
    /*
    public static final AttachmentType<ManaComponent> MANA_ATTACHMENT =
            AttachmentType.builder(ManaComponent::new)
                    .build();*/






}

