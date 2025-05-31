package com.minecraftquietus.quietus.item;

import com.minecraftquietus.quietus.util.TriFunction;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

public class QuietusItemProperties extends Item.Properties {

    public int projectilesPerShot;
    public QuietusItemProperties projectilesPerShot(int value) {
        if (value<1) throw new IllegalArgumentException("QuietusItemProperties: projectiles per shot must ≥ 1");
        this.projectilesPerShot = value;
        return this;
    }

    public TriFunction<Float,Integer,RandomSource,Float>[] rotOffsetCalc;
    @SuppressWarnings("unchecked")
    public QuietusItemProperties rotOffsetCalc(TriFunction<Float,Integer,RandomSource,Float> funcX, TriFunction<Float,Integer,RandomSource,Float> funcY) {
        this.rotOffsetCalc = new TriFunction[] {funcX,funcY};
        return this;
    }
   
}