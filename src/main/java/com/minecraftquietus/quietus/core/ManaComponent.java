package com.minecraftquietus.quietus.core;

import com.minecraftquietus.quietus.util.PlayerData;
import com.minecraftquietus.quietus.util.mana.Mana;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerEntityGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity.Server;
import net.neoforged.neoforge.common.util.INBTSerializable;

import static com.minecraftquietus.quietus.util.QuietusAttributes.MANA_REGEN_BONUS;
import static com.minecraftquietus.quietus.util.QuietusAttributes.MAX_MANA;
import static java.lang.Math.abs;

import java.util.Optional;

import javax.annotation.Nullable;

public class ManaComponent implements INBTSerializable<CompoundTag> {
    private int mana;
    private int maxMana=20;
    private double manaRate;
    private int Regen_bonus=5;
    private int Regen_delay=0;

    private final LivingEntity entity;
    @Nullable
    private final ServerPlayer serverPlayer;
    

    //private final int[] slotAnimOffsets = new int[40];

    /*
    // Constructor for Codec
    public ManaComponent(int currentMana, int maxMana) {
        mana = Math.min(mana, maxMana);
        this.maxMana = maxMana;
    }*/

    // Default constructor
    public ManaComponent(LivingEntity entity) {
        this.entity = entity;
        if (entity instanceof ServerPlayer serverPlayer) {
            this.serverPlayer = serverPlayer;
        } else {
            this.serverPlayer = null;
        }
    }
    /*
    public ManaComponent() {
        //LivingEntity livingEntity = QuietusCommonEvents.QuietusServerPlayer;
        //AttributeMap attribute_map = livingEntity.getAttributes();
        //this.maxMana= (int)attributes.getValue(MAX_MANA);
        this.maxMana=0;
    }*/

    /*public void initializePlayer(Player player, @Nullable ServerPlayer serverPlayer) {
        this.entity = player;
        if (serverPlayer != null) this.serverPlayer = serverPlayer;
    }*/

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


    public void tick() {
            if (this.serverPlayer != null) {if (this.serverPlayer.isCreative() || this.serverPlayer.isSpectator()) return;}
            checkManaAttributes();
            if(Regen_delay>0) Regen_delay--;
            if (!isFull() && Regen_delay<=0 && !entity.isDeadOrDying()) {
                setManaRegen();
                //lastRegenTime = player.tickCount;
            }
            else manaRate=0;
            if(mana > maxMana) {
                mana=maxMana;
                if (this.serverPlayer != null) PlayerData.ManapackToPlayer(serverPlayer,this);
            }
            //System.out.println("global"+globalBlinkEndTime);
        }

    // Getters
    public int getMana() { return mana;}
    public int getMaxMana() { return maxMana;}
    public LivingEntity getEntity() {return this.entity;}
    public Optional<ServerPlayer> getServerPlayer() {return Optional.of(this.serverPlayer);}

    private void checkManaAttributes()
    {
        
        int attributeMaxMana = (int)this.entity.getAttributes().getValue(MAX_MANA);
        int attributeManaRegen = (int)this.entity.getAttributes().getValue(MANA_REGEN_BONUS);
        if(maxMana != attributeMaxMana) {
            this.maxMana= attributeMaxMana;
            if (this.serverPlayer != null) PlayerData.ManapackToPlayer(this.serverPlayer,this);
        }
        if(Regen_bonus != attributeManaRegen) {
            Regen_bonus= attributeManaRegen;
        }
    }
        
        


    public void setManaRegen()
    {
        manaRate += (((double) maxMana /3 + 1 + getSneakBonus()+Regen_bonus) * ((mana/maxMana)*0.8+0.2)*1.15)*2.5;
        if(manaRate>=40)
        {
            int added_mana=(int)Math.floor(manaRate/40);
            manaRate-=40*(added_mana);
            addMana(added_mana);
        }
    }

    public double getSneakBonus()
    {
        if(this.entity.isShiftKeyDown()) return maxMana/3;
        /*
        if(abs(player.xCloak-player.xCloakO)<0.001)
        {
            //System.out.println(maxMana/3);
            return (double) maxMana /3;
        }*/
        //System.out.println(0);
        return 0;
    }
    public void addMana(int value) {
        //int prev = mana;
        mana +=value;
        if (this.serverPlayer != null) {
            PlayerData.ManapackToPlayer(this.serverPlayer,this);
        }

        // Trigger global blink when completing ANY slot
        /*
        if ((prev / 4) < (mana / 4)) {
            globalBlinkEndTime = player.tickCount + 2; // 0.2s blink

        }*/
        //System.out.println("global"+globalBlinkEndTime);
    }

    /* TODO
    public boolean consumeMana(int value, ServerPlayer player) { 
        if (value > Mana.getMana(player)) {
            return false;
        }
        else {

        }

    }*/


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

