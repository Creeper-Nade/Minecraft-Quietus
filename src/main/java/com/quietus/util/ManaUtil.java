package com.quietus.util;

import com.quietus.core.mana.ManaComponent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Utils for mana
 */
public class ManaUtil {

    public static ManaComponent get(LivingEntity entity) {
        return entity.getData(QuietusAttachments.MANA_ATTACHMENT.get());
    }
    
    public static int getMana(LivingEntity entity) {
        return get(entity).getMana();
    }
    public static int getMaxMana(LivingEntity entity) {
        return get(entity).getMaxMana();
    }

    public static void consumeMana(LivingEntity entity, int amount) {
        get(entity).consumeMana(amount);
    }

    public static void addMana(LivingEntity entity, int amount) {
        get(entity).addMana(amount);
    }

    public static void setMana(LivingEntity entity, int amount) {
        get(entity).setMana(amount);
    }

    public static void setMaxMana(LivingEntity entity, int amount) {
        get(entity).setMaxMana(amount);
    }
    

}
