package com.minecraftquietus.quietus.item;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class WeatheringCopperArmorItem extends Item implements WeatheringCopperItems {

    private static final float OXIDATION_CHANCE = (float)64/(float)1125 * (float)3/(float)4096; // according to minecraft.wiki copper blocks 64/1125 chance into pre-oxidization state for every random tick (default to 3 per 4096 for every tick). 

    // Codecs mapping item.properties and weatherstates (currently unused)
    //#region
    // Codec of Item.Properties: durability, enchantable, attributeModifiers, repairable (why? idk, since when oxidizing an instance of WeatheringCopperItems, only id, attributes and name component is changed while the rest are just copied onto the new item. This Codec is backed for future use.)
    public static final Codec<Item.Properties> PROPERTIES_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("durability", 0).forGetter(p -> 0), // Defaults to 0
        Codec.INT.optionalFieldOf("enchantable", 20).forGetter(p -> 20), // Defaults to 20 (same as oxidized_copper)
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
    public static final MapCodec<WeatheringCopperArmorItem> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            WeatheringCopperItems.WeatherState.WEATHERSTATE_CODEC.fieldOf("weathering_state").forGetter(WeatheringCopperArmorItem::getAge),
            PROPERTIES_CODEC.fieldOf("properties").forGetter(WeatheringCopperArmorItem::getProperties)
        ).apply(instance, WeatheringCopperArmorItem::new)
    );
    //#endregion
    
    private final WeatheringCopperItems.WeatherState weatherState; 

    private final Item.Properties savedProperties; // upon construction, save its properties
    public WeatheringCopperArmorItem(WeatherState weatherState, Item.Properties properties) {
        super(properties);
        this.savedProperties = properties; 
        this.weatherState = weatherState;
    }
    public WeatheringCopperArmorItem(String weatherStateString, Item.Properties properties) {
        this(WeatherState.valueOf(weatherStateString), properties);
    }

    public WeatheringCopperItems.WeatherState getAge() {
        return this.weatherState;
    }
    public float getChanceModifier() {
        return this.getAge() == WeatheringCopperArmorItem.WeatherState.UNAFFECTED ? 0.75F : 1.0F;
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

    public static boolean isValid(Item item) {
        return WeatheringCopperItems.isWeatherable(item)
            && !item.equals(Items.AIR)
            && item instanceof WeatheringCopperItem<?>;
    }
}




    // code copied from vanilla Minecraft WeatheringCopperFullBlock lol
    //Codec<Item.Properties> SAVED_PROPERTIES_CODEC = RecordCodecBuilder.
    /*public static final MapCodec<WeatheringCopperArmorItem> CODEC = RecordCodecBuilder.mapCodec(
    instance -> instance.group(
        WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(ChangeOverTimeBlock::getAge),
        Item.CODEC.fieldOf("properties").forGetter(Item::components)
    ).apply(instance, WeatheringCopperArmorItems::new)
    );*/

    /*public static final Codec<WeatheringCopperArmorItem> CODEC = RecordCodecBuilder.create(instance ->
    instance.group(
       Item.CODEC.fieldOf("item").forGetter(WeatheringCopperArmorItems::getItem) // Uses the custom codec
    ).apply(instance, WeatheringCopperArmorItems::new)
    );*/
/* 
    public static final MapCodec<WeatheringCopperArmorItem> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            WeatheringCopperItems.WeatherState.CODEC.fieldOf("weathering_state").forGetter(WeatheringCopperArmorItems::getAge),
            DataComponentMap.CODEC.optionalFieldOf("components", DataComponentMap.EMPTY).forGetter(WeatheringCopperArmorItems::components)
        ).apply(instance, WeatheringCopperArmorItems::new)
    );*/