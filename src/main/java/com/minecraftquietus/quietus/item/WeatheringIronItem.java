package com.minecraftquietus.quietus.item;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class WeatheringIronItem extends Item implements WeatheringIronItems {

    public static final float OXIDATION_CHANCE = (float)96/(float)854;
    public static final float OXIDATION_CHANCE_WARM = OXIDATION_CHANCE * 0.0f; // iron does not oxidize in atmosphere abscence of water.

    public static float getOxidationChance(boolean isWarm) {
        if (isWarm) {
            return OXIDATION_CHANCE_WARM;
        } else {
            return OXIDATION_CHANCE;
        }
    }
    
    private final WeatheringIronItems.WeatherState weatherState; 

    private final Item.Properties savedProperties; // upon construction, save its properties
    public WeatheringIronItem(WeatherState weatherState, Item.Properties properties) {
        super(properties);
        this.savedProperties = properties; 
        this.weatherState = weatherState;
    }
    public WeatheringIronItem(String weatherStateString, Item.Properties properties) {
        this(WeatherState.valueOf(weatherStateString), properties);
    }

    public WeatheringIronItems.WeatherState getAge() {
        return this.weatherState;
    }
    @Override
    public float getChanceModifier() {
        return this.getAge() == WeatheringIronItems.WeatherState.UNAFFECTED ? 0.9F : 1.0F;
    }
    @Override
    public boolean checkConditionsToWeather(ItemStack thisItem, ItemStack[] surroundingItems, RandomSource random, boolean isWarm) {
        if (random.nextFloat() < getOxidationChance(isWarm)) {
            return this.checkWeatheringSurroundings(thisItem, surroundingItems, random);
        }
        return false;
    }

    private Item.Properties getProperties() {
        return this.savedProperties;
    }

    public boolean isWeatherable() {
        return OXIDATION_MAP.containsKey(this);
    }
}
