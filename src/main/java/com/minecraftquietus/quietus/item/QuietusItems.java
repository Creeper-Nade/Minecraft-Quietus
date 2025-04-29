package com.minecraftquietus.quietus.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecraftquietus.quietus.Quietus.MODID;
import static com.minecraftquietus.quietus.block.QuietusBlocks.EXAMPLE_BLOCK;

import com.minecraftquietus.quietus.item.WeatheringCopperItems.WeatherState;
import com.minecraftquietus.quietus.item.equipment.QuietusArmorMaterials;


public class QuietusItems {
    // Create a Deferred Register to hold Items which will all be registered under the "quietus" namespace
    //CreeperNade: 1.21.2+ has changed register, use registerItem method in the format as below if you are to create an item

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    
    
    //public static final DeferredItem<Item> SPELUNKER_POTION = ITEMS.registerItem("spelunker_potion",Item::new,new Item.Properties());
    public static final DeferredItem<Item> HARDENED_FUR = ITEMS.registerItem("hardened_fur",Item::new,new Item.Properties());
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // copper armor & variants<-((normal),exposed,weathered,oxidized)
    public static final DeferredItem<Item> COPPER_BOOTS = registerCopperArmor("copper_boots", WeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.BOOTS);
    public static final DeferredItem<Item> COPPER_LEGGINGS = registerCopperArmor("copper_leggings", WeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> COPPER_CHESTPLATE = registerCopperArmor("copper_chestplate", WeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> COPPER_HELMET = registerCopperArmor("copper_helmet", WeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.HELMET);
    public static final DeferredItem<Item> EXPOSED_COPPER_BOOTS = registerCopperArmor("exposed_copper_boots", WeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.BOOTS);
    public static final DeferredItem<Item> EXPOSED_COPPER_LEGGINGS = registerCopperArmor("exposed_copper_leggings", WeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> EXPOSED_COPPER_CHESTPLATE = registerCopperArmor("exposed_copper_chestplate", WeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> EXPOSED_COPPER_HELMET = registerCopperArmor("exposed_copper_helmet", WeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.HELMET);
    public static final DeferredItem<Item> WEATHERED_COPPER_BOOTS = registerCopperArmor("weathered_copper_boots", WeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.BOOTS);
    public static final DeferredItem<Item> WEATHERED_COPPER_LEGGINGS = registerCopperArmor("weathered_copper_leggings", WeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> WEATHERED_COPPER_CHESTPLATE = registerCopperArmor("weathered_copper_chestplate", WeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> WEATHERED_COPPER_HELMET = registerCopperArmor("weathered_copper_helmet", WeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.HELMET);
    public static final DeferredItem<Item> OXIDIZED_COPPER_BOOTS = registerCopperArmor("oxidized_copper_boots", WeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.BOOTS);
    public static final DeferredItem<Item> OXIDIZED_COPPER_LEGGINGS = registerCopperArmor("oxidized_copper_leggings", WeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> OXIDIZED_COPPER_CHESTPLATE = registerCopperArmor("oxidized_copper_chestplate", WeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> OXIDIZED_COPPER_HELMET = registerCopperArmor("oxidized_copper_helmet", WeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.HELMET);
    
    
    private static DeferredItem<Item> registerCopperArmor(String name, WeatherState weatherState, ArmorMaterial armorMaterial, ArmorType armorType) {
        return ITEMS.registerItem(name, (properties -> new WeatheringCopperArmorItem(weatherState, new Item.Properties().humanoidArmor(armorMaterial, armorType).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel())))), new Item.Properties());
    }

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
