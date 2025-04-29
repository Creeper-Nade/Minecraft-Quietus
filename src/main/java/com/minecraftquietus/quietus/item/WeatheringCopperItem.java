package com.minecraftquietus.quietus.item;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface WeatheringCopperItem<T extends Enum<T>> {

    boolean checkConditionsToWeather(ItemStack thisItem, ItemStack[] surroundingItems, RandomSource random);

    float getChanceModifier();

    T getAge();
    
    default boolean checkWeatheringSurroundings(ItemStack thisItem, ItemStack[] surroundingItems, RandomSource random) {
        int this_age = this.getAge().ordinal();
        int items_higher_oxidized = 0;
        int items_equal_oxidized = 0;
        for(int i = 0; i < surroundingItems.length; ++i) {
            ItemStack item_checked = surroundingItems[i];
            if (!WeatheringCopperItems.isWeatherable(item_checked.getItem())) {continue;} // skip if this item is not weatherable (not registered in BiMap in WeatheringCopperItems)
            if (!item_checked.equals(thisItem)) /*not itself*/{
                if (item_checked.getItem() instanceof WeatheringCopperItem inst) {
                    Enum<?> item_checked_enum = inst.getAge();
                    if (this.getAge().getClass() == item_checked_enum.getClass()) {
                        int ordinal = item_checked_enum.ordinal();
                        if (ordinal < this_age) {
                            return false;
                        }
                        if (ordinal > this_age) {
                            items_higher_oxidized += item_checked.getCount();
                        } else {
                            items_equal_oxidized += item_checked.getCount();
                        }
                    }
                }
            }
        }
        float chance_surrounding = (float)(items_higher_oxidized + 1) / (float)(items_higher_oxidized + items_equal_oxidized + 1);
        float final_chance = chance_surrounding * chance_surrounding * this.getChanceModifier();
        return random.nextFloat() < final_chance;
    }



    public static boolean isValid(Item item) {
        if (WeatheringCopperItems.isWeatherable(item) && !item.equals(Items.AIR)) {
            return true;
        } else {return false;}
    }


}

