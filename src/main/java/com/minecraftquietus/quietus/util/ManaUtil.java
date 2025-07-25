package com.minecraftquietus.quietus.util;

import com.minecraftquietus.quietus.core.mana.ManaComponent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Utils for mana
 */
public final class ManaUtil {

    public static ManaComponent get(LivingEntity entity) {
        return entity.getData(QuietusAttachments.MANA_ATTACHMENT.get());
    }
    
    public static int getMana(LivingEntity entity) {
        return get(entity).getMana();
    }
    public static int getMaxMana(LivingEntity entity) {
        return get(entity).getMaxMana();
    }
    

}
