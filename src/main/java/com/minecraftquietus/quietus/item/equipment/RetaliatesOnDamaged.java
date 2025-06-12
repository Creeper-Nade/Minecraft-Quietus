package com.minecraftquietus.quietus.item.equipment;

import java.util.Map;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface RetaliatesOnDamaged {
    /**
    * invoked in {@link com.minecraftquietus.quietus.event.QuietusCommonEvents} when a LivingEntity has damage reducted by armor.
    * @param damage final damage (after-reduction) the entity took
    * @param armorMap map of EquipmentSlot to ItemStack of the entity's all equipments
    * @param slot the slot this item should be in of the wearer
    * @param wearer the LivingEntity wearing this 
    */
    public void onArmorHurt(float damage, Map<EquipmentSlot, ItemStack> armorMap, EquipmentSlot slot, LivingEntity wearer);
}
