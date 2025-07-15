package com.minecraftquietus.quietus.item;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface WeatheringItem<T extends Enum<T>> {

    boolean checkConditionsToWeather(ItemStack thisItem, ItemStack[] surroundingItems, RandomSource random, boolean isWarm);

    static boolean checkConditionsToWeatherExtra(ItemStack thisItem, ItemStack[] surroundingItems, RandomSource random, boolean isWarm) {
        if (isRegisteredExtraWeatheringItem(thisItem.getItem())) {
            if (random.nextFloat() < getExtraOxidationChance(thisItem.getItem(), isWarm)) {
                return checkWeatheringSurroundingsExtra(thisItem, surroundingItems, random);
            } else {return false;}
        } else {return false;}
    }

    float getChanceModifier();

    T getAge();

    // extra map object for storing non WeatheringItem instance Weather State, needs external registration.
    record CompositeWeatherstateOxidation(int[] weatherStateOrdinal, float oxidationChanceDirect, float oxidationChanceDirectWarm, Class<?> classWeatheringItem) {

    }
    
    static final Map<Item, CompositeWeatherstateOxidation> EXTRA_WEATHERING_ITEM = new HashMap<>();
    
    public static boolean isRegisteredExtraWeatheringItem(Item item) {
        return EXTRA_WEATHERING_ITEM.containsKey(item);
    }
    public static int[] getExtraWeatherState(Item item) {
        return EXTRA_WEATHERING_ITEM.get(item).weatherStateOrdinal();
    }
    public static float getExtraOxidationChance(Item item, boolean isWarm) {
        if (isWarm) {
            return EXTRA_WEATHERING_ITEM.get(item).oxidationChanceDirectWarm();
        } else {
            return EXTRA_WEATHERING_ITEM.get(item).oxidationChanceDirect();
        }
    }
    public static Class<?> getExtraClass(Item item) {
        return EXTRA_WEATHERING_ITEM.get(item).classWeatheringItem();
    }

    public static void registerExtraWeatheringItem(Item item, int[] weatherStatesOrdinal, float oxidationChance, float oxidationChanceWarm, Class<?> clazz) {
        EXTRA_WEATHERING_ITEM.put(item, new CompositeWeatherstateOxidation(weatherStatesOrdinal, oxidationChance, oxidationChanceWarm, clazz));
    }
    
    
    default boolean checkWeatheringSurroundings(ItemStack thisItem, ItemStack[] surroundingItems, RandomSource random) {
        int this_age = this.getAge().ordinal();
        Class<?> this_class = this.getAge().getClass();
        float chance_surrounding = calculateChanceBasedSurrounding(thisItem, this_age, this_class, surroundingItems, random);
        float final_chance = chance_surrounding * chance_surrounding * this.getChanceModifier();
        return random.nextFloat() < final_chance;
    }
    static boolean checkWeatheringSurroundingsExtra(ItemStack thisItem, ItemStack[] surroundingItems, RandomSource random) {
        int[] this_age_choose_from = getExtraWeatherState(thisItem.getItem());
        int this_age = this_age_choose_from[random.nextInt(0,this_age_choose_from.length)];
        Class<?> this_class = getExtraClass(thisItem.getItem());
        float chance_surrounding = calculateChanceBasedSurrounding(thisItem, this_age, this_class, surroundingItems, random);
        return random.nextFloat() < chance_surrounding;
    }
    static float calculateChanceBasedSurrounding(ItemStack thisItem, int thisAge, Class<?> thisWeatherStateClass, ItemStack[] surroundingItems, RandomSource random) {
        int items_higher_oxidized = 0;
        int items_equal_oxidized = 0;
        for(int i = 0; i < surroundingItems.length; ++i) { // check surrounding items one by one to calculate additional chance to oxidize based on surrounding.
            ItemStack item_checked = surroundingItems[i];
            if (!WeatheringItem.canWeather(item_checked.getItem())) {continue;} // skip if this item is not weatherable (should not accept items not registered in BiMap of that class of item_checked extending WeatheringItem)
            if (item_checked.equals(thisItem)) {continue;} // skip if this is itself
            if (item_checked.getItem() instanceof WeatheringItem inst) { // IF INSTANCE OF: if this item is an actual instance of WeatheringItem
                Enum<?> item_checked_enum = inst.getAge();
                if (thisWeatherStateClass == item_checked_enum.getClass()) { // if same WeatherState type
                    int checked_item_age = item_checked_enum.ordinal();
                    if (checked_item_age < thisAge) {
                        return 0.0f;
                    }
                    if (checked_item_age > thisAge) {
                        items_higher_oxidized += item_checked.getCount();
                    } else {
                        items_equal_oxidized += item_checked.getCount();
                    }
                } else { // if different WeatherState types, treat as equally oxidized item.
                    items_equal_oxidized += item_checked.getCount();
                }
            } else { // IF INSTANCE OF: case this item is not an actual instance of WeatheringItem
                if (isRegisteredExtraWeatheringItem(item_checked.getItem())) { // if it is registered in extra weather state
                    int[] checked_item_age_choose_from = getExtraWeatherState(item_checked.getItem());
                    int checked_item_age = checked_item_age_choose_from[random.nextInt(0,checked_item_age_choose_from.length)];
                    if (thisWeatherStateClass == getExtraClass(item_checked.getItem())) {
                        if (checked_item_age < thisAge) {
                            return 0.0f;
                        }
                        if (checked_item_age > thisAge) {
                            items_higher_oxidized += item_checked.getCount();
                        } else {
                            items_equal_oxidized += item_checked.getCount();
                        }
                    } else { // if different WeatherState types, treat as equally oxidized item.
                        items_equal_oxidized += item_checked.getCount();
                    }
                } else { // if it is not registered, then this item has no weather state hence not taken in calculation
                    continue;
                }
            }
        }
        return (float)(items_higher_oxidized + 1) / (float)(items_higher_oxidized + items_equal_oxidized + 1);
    }

    public boolean isWeatherable();

    public static boolean canWeather(Item item) {
        if (item instanceof WeatheringItem weatheringItem) {
            return weatheringItem.isWeatherable() && !item.equals(Items.AIR);
        } else if (isRegisteredExtraWeatheringItem(item) && !item.equals(Items.AIR)) {
            return WeatheringCopperItems.isWeatherable(item) 
            || WeatheringIronItems.isWeatherable(item);
        }
        return false;
    }
}

