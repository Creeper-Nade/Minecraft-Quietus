package com.quietus.core.mana;

import com.quietus.util.ManaUtil;
import com.quietus.util.PlayerClientPacketDistributor;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import static com.quietus.util.QuietusAttributes.MANA_REGEN_BONUS;
import static com.quietus.util.QuietusAttributes.MAX_MANA;


public class ManaComponent implements ValueIOSerializable {
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
    public void serialize(ValueOutput output) {
        output.putInt("mana", this.mana);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.mana = input.getIntOr("mana",10);
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
                    sendPacket(entity);
                }
            }
        }
        if(this.mana > this.maxMana) {
            this.mana = this.maxMana;
            sendPacket(entity);
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
            sendPacket(entity);
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
            addMana(added_mana, entity);
        }
    }

    public double getSneakBonus(LivingEntity entity)
    {
        if(entity.isShiftKeyDown()) {
            if(!is_fast_charging)
            {
                is_fast_charging=true;
                sendPacket(entity);
            }

            return maxMana / 3;
        }

        else
        {
            if(is_fast_charging)
            {
                is_fast_charging=false;
                sendPacket(entity);
            }
            return 0;
        }
        /* Check for movement
        if(abs(player.xCloak-player.xCloakO)<0.001)
        {
            //System.out.println(maxMana/3);
            return (double) maxMana /3;
        }*/
    }

    public void addMana(int value, LivingEntity entity) {
        /* CreeperNade:This is Unnecessary there is already a method in ManaHUDOverlay that detects this 👀, and as
        we said earlier do not directly link ManaComponent to ManaHudOverlay
        if (entity instanceof Player player) {
            boolean flagBlink = (blinkTicks > 0);
            if (entity instanceof Player && flagBlink) ManaHudOverlay.blinkContainers(blinkTicks, player);
        }*/
        this.mana += value;
        if (this.mana < 0) this.mana = 0;
        sendPacket(entity);
    }

    public boolean consumeMana(int value, LivingEntity entity) { 
        if (value > ManaUtil.getMana(entity)) {
            return false;
        }
        else {
            this.addMana(-value, entity);
            regenDelay= (int)(0.7*((1- (double) mana /maxMana)*120+45))/3;
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
        return this.is_fast_charging;
    }

    private void sendPacket(LivingEntity entity)
    {
        if (entity instanceof ServerPlayer serverPlayer) {
            PlayerClientPacketDistributor.sendManaPackToPlayer(serverPlayer,this);
        }
    }

}

