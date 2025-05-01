package com.minecraftquietus.quietus.event;

import com.electronwill.nightconfig.core.NullObject;
import com.minecraftquietus.quietus.item.WeatheringCopperArmorItem;
import com.minecraftquietus.quietus.item.WeatheringCopperItems;
import com.minecraftquietus.quietus.item.WeatheringIronArmorItem;
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
            if (event.getEntity() instanceof Player player) {
                //Player player = (Player)event.getEntity();
                ItemStack[] armorItems = { // ATTENTION: INVENTORY_SIZE by 1.21.5 vanilla is numerically equal to 36, and equipments indexes start from 36.
                    player.getInventory().getItem(EquipmentSlot.FEET.getIndex(Inventory.INVENTORY_SIZE)),
                    player.getInventory().getItem(EquipmentSlot.LEGS.getIndex(Inventory.INVENTORY_SIZE)),
                    player.getInventory().getItem(EquipmentSlot.CHEST.getIndex(Inventory.INVENTORY_SIZE)),
                    player.getInventory().getItem(EquipmentSlot.HEAD.getIndex(Inventory.INVENTORY_SIZE))
                };
                for (int i = 0; i < armorItems.length; ++i) { // armor items
                    Optional<Item> nextOptional = Optional.empty();
                    if (WeatheringItem.isValidToWeather(armorItems[i].getItem()) && armorItems[i].getItem() instanceof WeatheringItem<?> weatheringItem) {
                        if (weatheringItem.checkConditionsToWeather(armorItems[i], armorItems, RandomSource.create())) {
                            switch (armorItems[i].getItem()) { // flip through Oxidation_Map of all weathering items to find the next oxidation state of this item
                                case WeatheringCopperItems copperArmorItem -> {
                                    nextOptional = WeatheringCopperItems.getNext(armorItems[i].getItem());
                                }
                                case WeatheringIronItems ironArmorItem -> {
                                    nextOptional = WeatheringIronItems.getNext(armorItems[i].getItem());
                                }
                                case null -> {
                                    throw new NullPointerException("Item being checked is null");
                                }
                                default -> {
                                    nextOptional = Optional.ofNullable(Items.AIR); // defaults to change this item to air, if cannot determine what is the next oxidation of this item
                                }
                            }
                        }
                    } else {
                        if (WeatheringItem.isRegisteredExtraWeatheringItem(armorItems[i].getItem())) {
                            if (WeatheringCopperItems.OXIDATION_MAP.containsKey(armorItems[i].getItem())) {
                                nextOptional = WeatheringCopperItems.getNext(armorItems[i].getItem());
                            }
                            else if (WeatheringIronItems.OXIDATION_MAP.containsKey(armorItems[i].getItem())) {
                                nextOptional = WeatheringIronItems.getNext(armorItems[i].getItem());
                            }
                            else {continue;}
                        } else {continue;}
                    }
                    if (nextOptional.isPresent()) {
                        //LOGGER.info(armorItems[i].getItem().getName().getString() + " is oxidizing to " + nextOptional.get().asItem().getName().getString());
                        weatherArmorItem(player, nextOptional, armorItems[i], i+Inventory.INVENTORY_SIZE);
                    }
                }
            }
        }
    }
    @SuppressWarnings("unchecked") // supressing builder.set((DataComponentType<Object>) component.type(), (Object) component.value()); Unchecked Warning
    private static void weatherArmorItem(Player player, Optional<Item> newItemOptional, ItemStack oldStack, int slotIndex) {
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
        player.getInventory().setItem(slotIndex, newStack); 
        player.inventoryMenu.broadcastChanges();
    }
}
