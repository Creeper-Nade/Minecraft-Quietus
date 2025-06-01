package com.minecraftquietus.quietus.util.mana;

import com.minecraftquietus.quietus.core.ManaComponent;
import com.minecraftquietus.quietus.util.QuietusAttachments;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Utils for mana
 */
public class Mana {

    public static ManaComponent get(LivingEntity entity) {
        return entity.getData(QuietusAttachments.MANA_ATTACHMENT.get());
    }
    
    public static int getMana(LivingEntity entity) {
        return get(entity).getMana();
    }
    

}
