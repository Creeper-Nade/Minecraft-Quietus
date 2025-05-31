package com.minecraftquietus.quietus.util.mana;

import com.minecraftquietus.quietus.core.ManaComponent;
import com.minecraftquietus.quietus.util.QuietusAttachments;

import net.minecraft.world.entity.player.Player;

/**
 * Utils for mana
 */
public class Mana {

    public static ManaComponent get(Player player) {
        return player.getData(QuietusAttachments.MANA_ATTACHMENT.get());
    }

    public static Player getPlayer(ManaComponent component) {
        return component.getPlayer();
    }
    
    public static int getMana(Player player) {
        return get(player).getMana();
    }
    

}
