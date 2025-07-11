package com.minecraftquietus.quietus.item;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class WeatheringIronItem extends Item implements WeatheringIronItems {

    public static final float OXIDATION_CHANCE = (float)96/(float)854 * (float)3/(float)4096;

    public static float getOxidationChance() {
        return OXIDATION_CHANCE;
    }

    // Codecs mapping item.properties and weatherstates (currently unused)
    //#region
    // Codec of Item.Properties: durability, enchantable, attributeModifiers, repairable (why? idk, since when oxidizing an instance of WeatheringIronItems, only id, attributes and name component is changed while the rest are just copied onto the new item. This Codec is backed for future use.)
    public static final Codec<Item.Properties> PROPERTIES_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("durability", 0).forGetter(p -> 0), // Defaults to 0
        Codec.INT.optionalFieldOf("enchantable", 20).forGetter(p -> 20), // Defaults to
        ItemAttributeModifiers.CODEC.optionalFieldOf("attribute",ItemAttributeModifiers.EMPTY).forGetter(p -> ItemAttributeModifiers.EMPTY),
        Codec.BOOL.optionalFieldOf("repairable", true).forGetter(p -> true)
    ).apply(instance, (durability, enchantable, attributes, repairable) -> {
        Item.Properties propItem = new Item.Properties()
            .durability(durability)
            .enchantable(enchantable);
        if (!attributes.equals(ItemAttributeModifiers.EMPTY)) {
            propItem.attributes(attributes); 
        }
        if (!repairable) {
            propItem.setNoCombineRepair();
        }
        return propItem;
    }));
    public static final MapCodec<WeatheringIronItem> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            WeatheringIronItems.WeatherState.WEATHERSTATE_CODEC.fieldOf("weathering_state").forGetter(WeatheringIronItem::getAge),
            PROPERTIES_CODEC.fieldOf("properties").forGetter(WeatheringIronItem::getProperties)
        ).apply(instance, WeatheringIronItem::new)
    );
    //#endregion
    
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
    public float getChanceModifier() {
        return this.getAge() == WeatheringIronItems.WeatherState.UNAFFECTED ? 0.9F : 1.0F;
    }
    @Override
    public boolean checkConditionsToWeather(ItemStack thisItem, ItemStack[] surroundingItems, RandomSource random) {
        if (random.nextFloat() < OXIDATION_CHANCE) {
            return this.checkWeatheringSurroundings(thisItem, surroundingItems, random);
        } else {return false;}
    }

    private Item.Properties getProperties() {
        return this.savedProperties;
    }

    public boolean isWeatherable() {
        return OXIDATION_MAP.containsKey(this);
    }
}
