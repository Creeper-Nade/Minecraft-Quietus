package com.minecraftquietus.quietus.entity.monster;

import net.minecraft.world.entity.monster.RangedAttackMob;

public interface VolleyRangedAttackMob extends RangedAttackMob{
    /**
     * Volley getters and setting
     */
    int getVolley();
    void setVolley(int value);
    
    /**
     * Maximum volley getters and setting
     */
    int getVolleyMax();
    void setVolleyMax(int value);
}
