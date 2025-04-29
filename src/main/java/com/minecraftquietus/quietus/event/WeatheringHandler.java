package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.item.WeatheringCopperArmorItem;
import com.minecraftquietus.quietus.item.WeatheringCopperItems;
import com.minecraftquietus.quietus.item.WeatheringCopperItem;
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
            if (event.getEntity() instanceof Player) {
                Player player = (Player)event.getEntity();
                ItemStack[] armorItems = { // ATTENTION: INVENTORY_SIZE by 1.21.5 vanilla is numerically equal to 36, and equipments indexes start from 36.
                    player.getInventory().getItem(EquipmentSlot.FEET.getIndex(Inventory.INVENTORY_SIZE)),
                    player.getInventory().getItem(EquipmentSlot.LEGS.getIndex(Inventory.INVENTORY_SIZE)),
                    player.getInventory().getItem(EquipmentSlot.CHEST.getIndex(Inventory.INVENTORY_SIZE)),
                    player.getInventory().getItem(EquipmentSlot.HEAD.getIndex(Inventory.INVENTORY_SIZE))
                };
                for (int i = 0; i < armorItems.length; ++i) { // armor items
                    if (WeatheringCopperArmorItem.isValid(armorItems[i].getItem()) && armorItems[i].getItem() instanceof WeatheringCopperItem<?> weatheringItem) {
                        if (weatheringItem.checkConditionsToWeather(armorItems[i], armorItems, RandomSource.create())) {
                            Optional<Item> nextOptional = WeatheringCopperItems.getNext((Item)armorItems[i].getItem());
                            if (nextOptional.isPresent()) {
                                //LOGGER.info(armorItems[i].getItem().getName().getString() + " is oxidizing to " + nextOptional.get().asItem().getName().getString());
                                weatherArmorItem(player, nextOptional, armorItems[i], i+Inventory.INVENTORY_SIZE);
                            }
                        }
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
