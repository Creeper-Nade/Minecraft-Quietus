package com.minecraftquietus.quietus.item.equipment;

import java.util.Map;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent.ArmorEntry;

public interface RetaliatesOnDamaged {
    
    public float onArmorHurt(float damage, Map<EquipmentSlot, ArmorEntry> armorEntryMap, EquipmentSlot slot, LivingEntity wearer);
}
