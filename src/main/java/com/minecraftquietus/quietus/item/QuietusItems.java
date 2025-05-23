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

import com.minecraftquietus.quietus.item.WeatheringCopperItems.CopperWeatherState;
import com.minecraftquietus.quietus.item.WeatheringIronItems.IronWeatherState;
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
    public static final DeferredItem<Item> COPPER_BOOTS = registerCopperArmor("copper_boots", CopperWeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.BOOTS);
    public static final DeferredItem<Item> COPPER_LEGGINGS = registerCopperArmor("copper_leggings", CopperWeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> COPPER_CHESTPLATE = registerCopperArmor("copper_chestplate", CopperWeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> COPPER_HELMET = registerCopperArmor("copper_helmet", CopperWeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.HELMET);
    public static final DeferredItem<Item> EXPOSED_COPPER_BOOTS = registerCopperArmor("exposed_copper_boots", CopperWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.BOOTS);
    public static final DeferredItem<Item> EXPOSED_COPPER_LEGGINGS = registerCopperArmor("exposed_copper_leggings", CopperWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> EXPOSED_COPPER_CHESTPLATE = registerCopperArmor("exposed_copper_chestplate", CopperWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> EXPOSED_COPPER_HELMET = registerCopperArmor("exposed_copper_helmet", CopperWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.HELMET);
    public static final DeferredItem<Item> WEATHERED_COPPER_BOOTS = registerCopperArmor("weathered_copper_boots", CopperWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.BOOTS);
    public static final DeferredItem<Item> WEATHERED_COPPER_LEGGINGS = registerCopperArmor("weathered_copper_leggings", CopperWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> WEATHERED_COPPER_CHESTPLATE = registerCopperArmor("weathered_copper_chestplate", CopperWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> WEATHERED_COPPER_HELMET = registerCopperArmor("weathered_copper_helmet", CopperWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.HELMET);
    public static final DeferredItem<Item> OXIDIZED_COPPER_BOOTS = registerCopperArmor("oxidized_copper_boots", CopperWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.BOOTS);
    public static final DeferredItem<Item> OXIDIZED_COPPER_LEGGINGS = registerCopperArmor("oxidized_copper_leggings", CopperWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> OXIDIZED_COPPER_CHESTPLATE = registerCopperArmor("oxidized_copper_chestplate", CopperWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> OXIDIZED_COPPER_HELMET = registerCopperArmor("oxidized_copper_helmet", CopperWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.HELMET);

    // iron armor & variants<-(exposed,weathered,oxidized)
    public static final DeferredItem<Item> EXPOSED_IRON_BOOTS = registerIronArmor("exposed_iron_boots", IronWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_IRON, ArmorType.BOOTS);
    public static final DeferredItem<Item> EXPOSED_IRON_LEGGINGS = registerIronArmor("exposed_iron_leggings", IronWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_IRON, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> EXPOSED_IRON_CHESTPLATE = registerIronArmor("exposed_iron_chestplate", IronWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_IRON, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> EXPOSED_IRON_HELMET = registerIronArmor("exposed_iron_helmet", IronWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_IRON, ArmorType.HELMET);
    public static final DeferredItem<Item> WEATHERED_IRON_BOOTS = registerIronArmor("weathered_iron_boots", IronWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_IRON, ArmorType.BOOTS);
    public static final DeferredItem<Item> WEATHERED_IRON_LEGGINGS = registerIronArmor("weathered_iron_leggings", IronWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_IRON, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> WEATHERED_IRON_CHESTPLATE = registerIronArmor("weathered_iron_chestplate", IronWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_IRON, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> WEATHERED_IRON_HELMET = registerIronArmor("weathered_iron_helmet", IronWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_IRON, ArmorType.HELMET);
    public static final DeferredItem<Item> OXIDIZED_IRON_BOOTS = registerIronArmor("oxidized_iron_boots", IronWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_IRON, ArmorType.BOOTS);
    public static final DeferredItem<Item> OXIDIZED_IRON_LEGGINGS = registerIronArmor("oxidized_iron_leggings", IronWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_IRON, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> OXIDIZED_IRON_CHESTPLATE = registerIronArmor("oxidized_iron_chestplate", IronWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_IRON, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> OXIDIZED_IRON_HELMET = registerIronArmor("oxidized_iron_helmet", IronWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_IRON, ArmorType.HELMET);
    
    
    private static DeferredItem<Item> registerCopperArmor(String name, CopperWeatherState weatherState, ArmorMaterial armorMaterial, ArmorType armorType) {
        return ITEMS.registerItem(name, (properties -> new WeatheringCopperArmorItem(weatherState, new Item.Properties().humanoidArmor(armorMaterial, armorType).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel())))), new Item.Properties());
    }
    private static DeferredItem<Item> registerIronArmor(String name, IronWeatherState weatherState, ArmorMaterial armorMaterial, ArmorType armorType) {
        return ITEMS.registerItem(name, (properties -> new WeatheringIronArmorItem(weatherState, new Item.Properties().humanoidArmor(armorMaterial, armorType).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel())))), new Item.Properties());
    }

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
