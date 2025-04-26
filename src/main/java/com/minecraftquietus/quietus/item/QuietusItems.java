package com.minecraftquietus.quietus.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecraftquietus.quietus.Quietus.MODID;
import static com.minecraftquietus.quietus.block.QuietusBlocks.EXAMPLE_BLOCK;
import com.minecraftquietus.quietus.item.equipment.QuietusArmorMaterials;


public class QuietusItems {
    // Create a Deferred Register to hold Items which will all be registered under the "quietus" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    //CreeperNade: 1.21.2+ has changed register, use registerItem method in the format as below if you are to create an item
    //public static final DeferredItem<Item> SPELUNKER_POTION = ITEMS.registerItem("spelunker_potion",Item::new,new Item.Properties());
    public static final DeferredItem<Item> HARDENED_FUR = ITEMS.registerItem("hardened_fur",Item::new,new Item.Properties());
    // Creates a new BlockItem with the id "quietus:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);
    // Creates a new food item with the id "quietus:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    
    // copper armor
    public static final DeferredItem<Item> COPPER_BOOTS = ITEMS.registerItem("copper_boots", (properties -> new Item (new Item.Properties().humanoidArmor(QuietusArmorMaterials.COPPER, ArmorType.BOOTS).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel())))), new Item.Properties());
    public static final DeferredItem<Item> COPPER_LEGGINGS = ITEMS.registerItem("copper_leggings", (properties -> new Item (new Item.Properties().humanoidArmor(QuietusArmorMaterials.COPPER, ArmorType.LEGGINGS).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel())))), new Item.Properties());
    public static final DeferredItem<Item> COPPER_CHESTPLATE = ITEMS.registerItem("copper_chestplate", (properties -> new Item (new Item.Properties().humanoidArmor(QuietusArmorMaterials.COPPER, ArmorType.CHESTPLATE).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel())))), new Item.Properties());
    public static final DeferredItem<Item> COPPER_HELMET = ITEMS.registerItem("copper_helmet", (properties -> new Item (new Item.Properties().humanoidArmor(QuietusArmorMaterials.COPPER, ArmorType.HELMET).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel())))), new Item.Properties());
    
    

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
