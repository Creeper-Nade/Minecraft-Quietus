package com.minecraftquietus.quietus.item;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface WeatheringItem<T extends Enum<T>> {

    boolean checkConditionsToWeather(ItemStack thisItem, ItemStack[] surroundingItems, RandomSource random);

    float getChanceModifier();

    T getAge();

    // extra map object for storing non WeatheringItem instance Weather State, needs external registration.
    record CompositeWeatherstateOxidation(int[] weatherStateOrdinal, float oxidationChanceDirect) {}
    static final Map<Item, CompositeWeatherstateOxidation> EXTRA_WEATHERING_ITEM = new HashMap<>();
    
    public static boolean isRegisteredExtraWeatheringItem(Item item) {
        return EXTRA_WEATHERING_ITEM.containsKey(item);
    }
    public static int[] getExtraWeatherState(Item item) {
        return EXTRA_WEATHERING_ITEM.get(item).weatherStateOrdinal();
    }
    public static float getExtraOxidationChance(Item item) {
        return EXTRA_WEATHERING_ITEM.get(item).oxidationChanceDirect();
    }

    public static void registerExtraItemWeatherstate(Item a, int[] b, float c) {
        EXTRA_WEATHERING_ITEM.put(a, new CompositeWeatherstateOxidation(b,c));
    }
    
    
    default boolean checkWeatheringSurroundings(ItemStack thisItem, ItemStack[] surroundingItems, RandomSource random) {
        int this_age = this.getAge().ordinal();
        int items_higher_oxidized = 0;
        int items_equal_oxidized = 0;
        for(int i = 0; i < surroundingItems.length; ++i) { // check surrounding items one by one to calculate additional chance to oxidize based on surrounding. Only items instance of WeatheringItem and can oxidize will be counted
            ItemStack item_checked = surroundingItems[i];
            if (!WeatheringItem.isValidToWeather(item_checked.getItem())) {continue;} // skip if this item is not weatherable (should not accept items not registered in BiMap of that class of item_checked extending WeatheringItem)
            if (!item_checked.equals(thisItem)) {continue;} // skip if this is itself
            if (item_checked.getItem() instanceof WeatheringItem inst) { // IF INSTANCE OF: if this item is an acutal instance of WeatheringItem
                Enum<?> item_checked_enum = inst.getAge();
                if (this.getAge().getClass() == item_checked_enum.getClass()) { // if same WeatherState type
                    int checked_item_age = item_checked_enum.ordinal();
                    if (checked_item_age < this_age) {
                        return false;
                    }
                    if (checked_item_age > this_age) {
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
                    if (checked_item_age < this_age) {
                        return false;
                    }
                    if (checked_item_age > this_age) {
                        items_higher_oxidized += item_checked.getCount();
                    } else {
                        items_equal_oxidized += item_checked.getCount();
                    }
                } else { // if it is not registered, then this item has no weather state hence not taken in calculation
                    continue;
                }
            }
        }
        float chance_surrounding = (float)(items_higher_oxidized + 1) / (float)(items_higher_oxidized + items_equal_oxidized + 1);
        float final_chance = chance_surrounding * chance_surrounding * this.getChanceModifier();
        return random.nextFloat() < final_chance;
    }


    public boolean isWeatherable();

    public static boolean isValidToWeather(Item item) {
        if (item instanceof WeatheringItem weatheringItem) {
            return weatheringItem.isWeatherable() && !item.equals(Items.AIR);
        } else {return false;}
    }


}

