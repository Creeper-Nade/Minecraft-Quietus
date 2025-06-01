package com.minecraftquietus.quietus.core;

import com.minecraftquietus.quietus.util.PlayerData;
import com.minecraftquietus.quietus.util.mana.Mana;
import com.minecraftquietus.quietus.util.mana.ManaHudOverlay;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity.Server;
import net.neoforged.neoforge.common.util.INBTSerializable;

import static com.minecraftquietus.quietus.util.QuietusAttributes.MANA_REGEN_BONUS;
import static com.minecraftquietus.quietus.util.QuietusAttributes.MAX_MANA;


public class ManaComponent implements INBTSerializable<CompoundTag> {
    private int mana;
    private int maxMana = 20;
    private boolean is_fast_charging=false;
    private double manaRate;
    private int regenBonus = 5;
    private int regenDelay = 0;

    //private final int[] slotAnimOffsets = new int[40];

    /*
    // Constructor for Codec
    public ManaComponent(int currentMana, int maxMana) {
        mana = Math.min(mana, maxMana);
        this.maxMana = maxMana;
    }*/

    // Default constructor
    /*
    public ManaComponent() {
        //LivingEntity livingEntity = QuietusCommonEvents.QuietusServerPlayer;
        //AttributeMap attribute_map = livingEntity.getAttributes();
        //this.maxMana= (int)attributes.getValue(MAX_MANA);
        this.maxMana=0;
    }*/

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("mana", this.mana);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.mana = nbt.getIntOr("mana",10);
    }


    public void tick(LivingEntity entity) {

        checkManaAttributes(entity);
        if (!entity.level().isClientSide()) {
            if (this.regenDelay>0) this.regenDelay--;
            if (!isFull() && regenDelay<=0 && !entity.isDeadOrDying()) {
                regenMana(entity);
                //lastRegenTime = player.tickCount;
            }
            else {
                manaRate=0;
                if (is_fast_charging) {
                    is_fast_charging=false;
                    if (entity instanceof ServerPlayer serverPlayer) {
                        PlayerData.manapackToPlayer(serverPlayer,this);
                    }
                }
            }
        }
        if(this.mana > this.maxMana) {
            this.mana = this.maxMana;
            if (entity instanceof ServerPlayer serverPlayer) PlayerData.manapackToPlayer(serverPlayer,this);
        }
        //System.out.println("global"+globalBlinkEndTime);
    }

    private void checkManaAttributes(LivingEntity entity)
    {
        int attribute_max_mana = (int)entity.getAttributes().getValue(MAX_MANA);
        int attribute_mana_regen_bonus = (int)entity.getAttributes().getValue(MANA_REGEN_BONUS);
        if(this.maxMana != attribute_max_mana)
        {
            this.maxMana = attribute_max_mana;
            if (entity instanceof ServerPlayer serverPlayer) PlayerData.manapackToPlayer(serverPlayer,this);
        }
        if(regenBonus != attribute_mana_regen_bonus) {
            regenBonus = attribute_mana_regen_bonus;
        }
            
    }

    public void regenMana(LivingEntity entity)
    {
        this.manaRate += (((double) this.maxMana /3 + 1 + getSneakBonus(entity)+this.regenBonus) * ((this.mana/this.maxMana)*0.8+0.2)*1.15)*2.5;
        if (this.manaRate >= 40)
        {
            int added_mana= (int)Math.floor(this.manaRate/40);
            this.manaRate -= 40*(added_mana);
            addMana(added_mana, entity, 0);
        }
    }

    public double getSneakBonus(LivingEntity entity)
    {
        if(entity.isShiftKeyDown()) {
            if(!is_fast_charging)
            {
                is_fast_charging=true;
                if (entity instanceof ServerPlayer serverPlayer) {
                    PlayerData.manapackToPlayer(serverPlayer,this);
                }
            }

            return maxMana / 3;
        }
        else return 0;
        /* Check for movement
        if(abs(player.xCloak-player.xCloakO)<0.001)
        {
            //System.out.println(maxMana/3);
            return (double) maxMana /3;
        }*/
    }

    public void addMana(int value, LivingEntity entity, int blinkTicks) {
        if (entity instanceof Player player) {
            boolean flagBlink = (blinkTicks > 0);
            if (entity instanceof Player && flagBlink) ManaHudOverlay.blinkContainers(blinkTicks, player);
        }
        this.mana += value;
        if (this.mana < 0) this.mana = 0;
        if (entity instanceof ServerPlayer serverPlayer) PlayerData.manapackToPlayer(serverPlayer, this);
    }

    public boolean consumeMana(int value, LivingEntity entity) { 
        if (value > Mana.getMana(entity)) {
            return false;
        }
        else {
            this.addMana(-value, entity, 4);
            return true;
        }
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

    // Getters
    public int getMana() {
        return this.mana;
    }
    public int getMaxMana() {
        return this.maxMana;
    }
    public boolean getSpeedChargeStatus() {
        return is_fast_charging;
    }




}

