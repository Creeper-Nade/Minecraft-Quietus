package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.item.WeatheringCopperItems;
import com.minecraftquietus.quietus.item.WeatheringIronItems;
import com.minecraftquietus.quietus.item.WeatheringItem;
import com.mojang.logging.LogUtils;

import java.util.Optional;

import org.slf4j.Logger;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import static com.minecraftquietus.quietus.Quietus.MODID;

@EventBusSubscriber(modid=MODID)
public class WeatheringHandler {
    private static final Logger LOGGER = LogUtils.getLogger();


    @SubscribeEvent
    public static void onServerTick(EntityTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) {
            if (event.getEntity() instanceof LivingEntity livingEntity) {
                ItemStack[] armorItems = { // ATTENTION: INVENTORY_SIZE by 1.21.5 vanilla is numerically equal to 36, and equipments indexes start from 36.
                    livingEntity.getItemBySlot(EquipmentSlot.FEET),
                    livingEntity.getItemBySlot(EquipmentSlot.LEGS),
                    livingEntity.getItemBySlot(EquipmentSlot.CHEST),
                    livingEntity.getItemBySlot(EquipmentSlot.HEAD)
                }; EquipmentSlot[] armorSlots = {EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD};
                for (int i = 0; i < armorItems.length; ++i) { // armor items
                    if (!WeatheringItem.isValidToWeather(armorItems[i].getItem())) {continue;} // skip if this item is not weatherable
                    Optional<Item> nextOptional = checkAndGetNextWeatherItem(armorItems[i], armorItems);
                    if (nextOptional.isPresent()) {
                        //LOGGER.info(armorItems[i].getItem().getName().getString() + " is oxidizing to " + nextOptional.get().asItem().getName().getString());
                        weatherArmorItem(livingEntity, nextOptional, armorItems[i], armorSlots[i]);
                    }
                }
            } /* // Deepseek is liar; ArmorStand is a child class of LivingEntity. Below code is deprecated and can be used in case of exceptional (non-LivingEntity) entities that are able to equip armor in the future
            else 
            if (event.getEntity() instanceof ArmorStand armorStand) {
                ItemStack[] armorItems = { // ATTENTION: INVENTORY_SIZE by 1.21.5 vanilla is numerically equal to 36, and equipments indexes start from 36.
                    armorStand.getItemBySlot(EquipmentSlot.FEET),
                    armorStand.getItemBySlot(EquipmentSlot.LEGS),
                    armorStand.getItemBySlot(EquipmentSlot.CHEST),
                    armorStand.getItemBySlot(EquipmentSlot.HEAD)
                }; EquipmentSlot[] armorSlots = {EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD};
                for (int i = 0; i < armorItems.length; ++i) { // armor items
                    if (!WeatheringItem.isValidToWeather(armorItems[i].getItem())) {continue;} // skip if this item is not weatherable
                    Optional<Item> nextOptional = checkAndGetNextWeatherItem(armorItems[i], armorItems);
                    if (nextOptional.isPresent()) {
                        //LOGGER.info(armorItems[i].getItem().getName().getString() + " is oxidizing to " + nextOptional.get().asItem().getName().getString());
                        weatherArmorItem(armorStand, nextOptional, armorItems[i], armorSlots[i]);
                    }
                }
            }*/
        }
    }

    private static Optional<Item> checkAndGetNextWeatherItem(ItemStack armorItem, ItemStack[] armorItems) {
        if (armorItem.getItem() instanceof WeatheringItem<?> weatheringItem) {
            if (weatheringItem.checkConditionsToWeather(armorItem, armorItems, RandomSource.create())) {
                switch (armorItem.getItem()) { // flip through Oxidation_Map of all weathering items to find the next oxidation state of this item
                    case WeatheringCopperItems copperArmorItem -> {
                        return WeatheringCopperItems.getNext(armorItem.getItem());
                    }
                    case WeatheringIronItems ironArmorItem -> {
                        return WeatheringIronItems.getNext(armorItem.getItem());
                    }
                    case null -> {
                        throw new NullPointerException("Item being checked is null");
                    }
                    default -> {
                        return Optional.ofNullable(Items.AIR); // defaults to change this item to air, if cannot determine what is the next oxidation of this item
                    }
                }
            }
        } else {
            if (WeatheringItem.isRegisteredExtraWeatheringItem(armorItem.getItem())) {
                if (WeatheringItem.checkConditionsToWeatherExtra(armorItem, armorItems, RandomSource.create())) {
                    if (WeatheringCopperItems.OXIDATION_MAP.containsKey(armorItem.getItem())) {
                        return WeatheringCopperItems.getNext(armorItem.getItem());
                    }
                    else if (WeatheringIronItems.OXIDATION_MAP.containsKey(armorItem.getItem())) {
                        return WeatheringIronItems.getNext(armorItem.getItem());
                    }
                }
            }
        }
        // if none above match, return nothing
        return Optional.empty();
    }

    @SuppressWarnings("unchecked") // supressing builder.set((DataComponentType<Object>) component.type(), (Object) component.value()); Unchecked Warning
    private static void weatherArmorItem(LivingEntity entity, Optional<Item> newItemOptional, ItemStack oldStack, EquipmentSlot slot) {
        // copying components from old item, all but attribute modifiers and item name
        ItemStack newStack = new ItemStack(newItemOptional.get());
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        oldStack.getComponents().forEach((component) -> { // newStack does not inherit the attribute modifiers, item name and item model
            if (!component.type().equals(DataComponents.ATTRIBUTE_MODIFIERS) 
             && !component.type().equals(DataComponents.ITEM_NAME) 
             && !component.type().equals(DataComponents.ITEM_MODEL)) {
                builder.set((DataComponentType<Object>) component.type(), (Object) component.value());
            }
        });
        newStack.applyComponents(builder.build());
        // setting the new item
        if (entity instanceof Player player) {
            player.getInventory().setItem(slot.getIndex(Inventory.INVENTORY_SIZE), newStack); // using this specifically for players can set items 'silently'; as for armor without triggering equipment sound
            player.inventoryMenu.broadcastChanges();
        } else {
            entity.setItemSlot(slot, newStack); 
        }
    }
    //@SuppressWarnings("unchecked") // supressing builder.set((DataComponentType<Object>) component.type(), (Object) component.value()); Unchecked Warning
    /*private static void weatherArmorItem(ArmorStand armorStand, Optional<Item> newItemOptional, ItemStack oldStack, EquipmentSlot slot) {
        // copying components from old item, all but attribute modifiers and item name
        ItemStack newStack = new ItemStack(newItemOptional.get());
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        oldStack.getComponents().forEach((component) -> { // newStack does not inherit the attribute modifiers, item name and item model
            if (!component.type().equals(DataComponents.ATTRIBUTE_MODIFIERS) 
             && !component.type().equals(DataComponents.ITEM_NAME) 
             && !component.type().equals(DataComponents.ITEM_MODEL)) {
                builder.set((DataComponentType<Object>) component.type(), (Object) component.value());
            }
        });
        newStack.applyComponents(builder.build());
        // setting the new item
        armorStand.setItemSlot(slot, newStack); 
    }*/
}
