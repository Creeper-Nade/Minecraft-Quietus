package com.minecraftquietus.quietus.item;

import net.minecraft.world.food.FoodProperties;

public class QuietusFoods {
    
    public static final FoodProperties MOLD = new FoodProperties.Builder().nutrition(0).saturationModifier(0.0f).build();

    public static final FoodProperties MOLD_BUCKET = new FoodProperties.Builder().nutrition(1).saturationModifier(0.0f).build();

    public static final FoodProperties MOLD_BOWL = new FoodProperties.Builder().nutrition(1).saturationModifier(0.0f).build();

    public static final FoodProperties YOGHURT_BUCKET = new FoodProperties.Builder().nutrition(5).saturationModifier(0.6f).build();

    public static final FoodProperties CHEESE_BUCKET = new FoodProperties.Builder().nutrition(4).saturationModifier(0.75f).build();


    public static FoodProperties init() {
        return MOLD;
    }
}
