package com.minecraftquietus.quietus.item;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

public interface WeatheringIronItems extends WeatheringItem<WeatheringIronItems.IronWeatherState> {

    static final BiMap<Item, Item> OXIDATION_MAP = HashBiMap.create();

    Supplier<BiMap<Item, Item>> NEXT_BY_ITEM = Suppliers.memoize(
        () -> OXIDATION_MAP
    );
    Supplier<BiMap<Item, Item>> PREVIOUS_BY_ITEM = Suppliers.memoize(() -> ((BiMap<Item,Item>)NEXT_BY_ITEM.get()).inverse());


    // Registered on mod event bus (after potentially any mod items added)
    public static void registerWeathering(Item c, Item n) {
        OXIDATION_MAP.put(c, n);
    }

    public static Optional<Item> getNext(Item c) {
        return Optional.ofNullable((Item)((BiMap<Item,Item>)NEXT_BY_ITEM.get()).get(c));
    }
    public static Optional<Item> getPrevious(Item c) {
        return Optional.ofNullable((Item)((BiMap<Item,Item>)PREVIOUS_BY_ITEM.get()).get(c));
    }
    
    public static boolean isWeatherable(Item item) {
        return OXIDATION_MAP.containsKey(item);
    }
    public boolean isWeatherable();


    public static enum IronWeatherState implements StringRepresentable {
        UNAFFECTED("unaffected"),
        EXPOSED("exposed"),
        WEATHERED("weathered"),
        OXIDIZED("oxidized");
        private IronWeatherState(final String nameString) {
            this.name = nameString;
        }
        
        public static final Codec<WeatheringIronItems.IronWeatherState> WEATHERSTATE_CODEC = StringRepresentable.fromEnum(WeatheringIronItems.IronWeatherState::values);
        private final String name;


        @Override
        public String getSerializedName() {
            return this.name;
        }

    }
}
