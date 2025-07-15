package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.item.WeatheringCopperItems;
import com.minecraftquietus.quietus.item.WeatheringIronItems;
import com.minecraftquietus.quietus.item.WeatheringItem;
import com.minecraftquietus.quietus.util.container.ContainerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractChestBoat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import static net.minecraft.world.level.GameRules.RULE_RANDOMTICKING;

import static com.minecraftquietus.quietus.Quietus.MODID;

@EventBusSubscriber(modid = MODID)
/**
 * WeatheringHandler is responsible for all weathering-related processes, 
 * desired to weather world items  (including from containers and entities) spontaneously over time,
 * whether oxidizing or food rotting in Quietus.
 */
public class WeatheringHandler {

    private static final Set<BaseContainerBlockEntity> LOADED_CONTAINERS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @SubscribeEvent
    /**
     * Does entities equipments weathering 
     * (for Player inventories, for every LivingEntity armor they are wearing and held item)
     */
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        Level level = entity.level();
        int randomTickSpeed = 3; // default value in case server is null
        if (!Objects.isNull(level.getServer())) randomTickSpeed = level.getServer().getGameRules().getRule(RULE_RANDOMTICKING).get();
        float tick_chance = (float)randomTickSpeed / (float)4096; // 4096 blocks per chunk
        if (!level.isClientSide() 
         && entity.getRandom().nextFloat() < tick_chance) { // server side && also apply chances with random ticking
            /* LivingEntity */
            if (entity instanceof LivingEntity livingEntity) {
                /** Armor oxidation */
                ItemStack[] armorItems = { // ATTENTION: INVENTORY_SIZE by 1.21.5 vanilla is numerically equal to 36, and equipments indexes start from 36.
                    livingEntity.getItemBySlot(EquipmentSlot.FEET),
                    livingEntity.getItemBySlot(EquipmentSlot.LEGS),
                    livingEntity.getItemBySlot(EquipmentSlot.CHEST),
                    livingEntity.getItemBySlot(EquipmentSlot.HEAD)
                }; EquipmentSlot[] armorSlots = {EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD};
                for (int i = 0; i < armorItems.length; ++i) { // armor items
                    if (WeatheringItem.canWeather(armorItems[i].getItem())) { // only if this item is not weatherable
                        Optional<Item> nextOptional = checkAndGetNextWeatherItem(armorItems[i], armorItems, level.dimensionType().ultraWarm());
                        if (nextOptional.isPresent()) {
                            //LOGGER.info(armorItems[i].getItem().getName().getString() + " is oxidizing to " + nextOptional.get().asItem().getName().getString());
                            replaceArmorItem(livingEntity, makeNewWeatheredStack(nextOptional, armorItems[i]), armorSlots[i]);
                        }
                    }
                }
                if (livingEntity instanceof Player player) { // also checks for player inventory for oxidizing
                    Inventory inventory = player.getInventory();
                    for (int i = 0; i < Inventory.INVENTORY_SIZE; i ++) {
                        ItemStack itemstack = inventory.getItem(i);
                        if (WeatheringItem.canWeather(itemstack.getItem())) {
                            int perRow = Inventory.getSelectionSize();
                            List<ItemStack> surroundingItems = new ArrayList<>(8);
                            /** surrounding items set to all items visibly neighboring this item within the inventory GUI */
                            for (int a = -1; a <= 1; a++) { // a for row offset
                                for (int b = -1; b <= 1; b++) { // b for horizontal offset
                                    int index = i + a*perRow + b;
                                    if (i < perRow) { // however, hotbar oxidizes seperately
                                        if (a != 0) continue;
                                    } else {
                                        if (index < perRow) continue;
                                    }
                                    if (index >= 0 
                                    && index < Inventory.INVENTORY_SIZE 
                                    && index != i
                                    && Math.floorMod(i, perRow) + b >= 0
                                    && Math.floorMod(i, perRow) + b < perRow) 
                                        surroundingItems.add(inventory.getItem(index));
                                }
                            }
                            Optional<Item> nextOptional = checkAndGetNextWeatherItem(itemstack, surroundingItems.toArray(new ItemStack[0]), level.dimensionType().ultraWarm());
                            if (nextOptional.isPresent()) {
                                replaceInventoryItem(player, makeNewWeatheredStack(nextOptional, itemstack), i);
                            }
                        }
                    }
                }
            } else
            /* ContainerEntity (e.g. ChestBoat, MinecartChest) */
            if (entity instanceof ContainerEntity containerEntity) {
                int perRow = 9;
                int containerSize = containerEntity.getContainerSize();
                for (int i = 0; i < containerSize; i++) {
                    ItemStack itemstack = containerEntity.getItem(i);
                    if (WeatheringItem.canWeather(itemstack.getItem())) {
                        ItemStack[] surroundingItems = ContainerUtil.getSurroundingItems(i, 1, 1, containerEntity, perRow, false);
                        Optional<Item> nextOptional = checkAndGetNextWeatherItem(itemstack, surroundingItems, level.dimensionType().ultraWarm());
                        if (nextOptional.isPresent()) {
                            containerEntity.setItem(i, makeNewWeatheredStack(nextOptional, itemstack));
                        }
                    }
                }
            } else
            /* ItemFrame */
            if (entity instanceof ItemFrame itemFrame) {
                ItemStack itemstack = itemFrame.getItem();
                if (WeatheringItem.canWeather(itemstack.getItem())) {
                    ItemStack[] surroundingItems = new ItemStack[0]; // it displays just 1 item!
                    Optional<Item> nextOptional = checkAndGetNextWeatherItem(itemstack, surroundingItems, level.dimensionType().ultraWarm());
                    if (nextOptional.isPresent()) {
                        itemFrame.setItem(makeNewWeatheredStack(nextOptional, itemstack));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    /**
     * Handles marked containers weathering, which should have all containers marked
     * The rate is doubled if level's DimensionType boolean ultraWarm == TRUE. 
     * @param event LevelTickEvent.Pre
     */
    public static void onLevelTick(LevelTickEvent.Pre event) {
        Level level = event.getLevel();
        if (!level.isClientSide()) {
            int randomTickSpeed = 3; // default value in case server is null
            if (!Objects.isNull(level.getServer())) randomTickSpeed = level.getServer().getGameRules().getRule(RULE_RANDOMTICKING).get();
            float tick_chance = (float)randomTickSpeed / (float)4096; // 4096 blocks per chunk
            for (BaseContainerBlockEntity container : LOADED_CONTAINERS) {
                BlockEntity get_block_entity = level.getBlockEntity(container.getBlockPos());
                if (!Objects.isNull(get_block_entity) && get_block_entity.equals(container)) { // only operate this block entity if it is in this level
                    if (container.isRemoved()) {
                        LOADED_CONTAINERS.remove(container);
                        continue;
                    }
                    if (level.getRandom().nextFloat() < tick_chance) { // accounts for randomTickSpeed gamerule
                        int perRow = 1;
                        if (container.getType() == BlockEntityType.CHEST
                        || container.getType() == BlockEntityType.TRAPPED_CHEST
                        || container.getType() == BlockEntityType.BARREL) {
                            perRow = 9;
                        } else {
                            continue;
                        }
                        int containerSize = container.getContainerSize();
                        for (int i = 0; i < containerSize; i++) {
                            ItemStack itemstack = container.getItem(i);
                            if (WeatheringItem.canWeather(itemstack.getItem())) {
                                ItemStack[] surroundingItems = ContainerUtil.getSurroundingItems(i, 1, 1, container, perRow, false);
                                Optional<Item> nextOptional = checkAndGetNextWeatherItem(itemstack, surroundingItems, level.dimensionType().ultraWarm());
                                if (nextOptional.isPresent()) {
                                    container.setItem(i, makeNewWeatheredStack(nextOptional, itemstack));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LevelChunk chunk = event.getChunk();
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof BaseContainerBlockEntity container) {
                LOADED_CONTAINERS.add(container);
            }
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        LevelChunk chunk = event.getChunk();
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof BaseContainerBlockEntity container) {
                LOADED_CONTAINERS.remove(container);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlacement(BlockEvent.EntityPlaceEvent event) {
        //BlockEntity blockEntity = event.getBlockSnapshot().recreateBlockEntity(event.getLevel().registryAccess());
        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (blockEntity instanceof BaseContainerBlockEntity container) {
            LOADED_CONTAINERS.add(container);
        }
    }

    private static Optional<Item> checkAndGetNextWeatherItem(ItemStack itemstack, ItemStack[] surroundingItems, boolean isWarm) {
        if (itemstack.getItem() instanceof WeatheringItem<?> weatheringItem) { // is WeatheringItem instance
            if (weatheringItem.checkConditionsToWeather(itemstack, surroundingItems, RandomSource.create(), isWarm)) {
                switch (itemstack.getItem()) { // flip through Oxidation_Map of all weathering items to find the next oxidation state of this item
                    case WeatheringCopperItems copperArmorItem -> {
                        return WeatheringCopperItems.getNext(itemstack.getItem());
                    }
                    case WeatheringIronItems ironArmorItem -> {
                        return WeatheringIronItems.getNext(itemstack.getItem());
                    }
                    case null -> {
                        throw new NullPointerException("Item being checked is null");
                    }
                    default -> {
                        return Optional.ofNullable(Items.AIR); // defaults to change this item to air, if cannot determine what is the next oxidation of this item
                    }
                }
            }
        } else { // is not WeatheringItem instance, but any other instance
            if (WeatheringItem.isRegisteredExtraWeatheringItem(itemstack.getItem())) {
                if (WeatheringItem.checkConditionsToWeatherExtra(itemstack, surroundingItems, RandomSource.create(), isWarm)) {
                    if (WeatheringCopperItems.OXIDATION_MAP.containsKey(itemstack.getItem())) {
                        return WeatheringCopperItems.getNext(itemstack.getItem());
                    }
                    else if (WeatheringIronItems.OXIDATION_MAP.containsKey(itemstack.getItem())) {
                        return WeatheringIronItems.getNext(itemstack.getItem());
                    }
                }
            }
        }
        // if none above match, return nothing
        return Optional.empty();
    }

    private static ItemStack makeNewWeatheredStack(Optional<Item> nextItemOptional, ItemStack oldStack) {
        // copying components from old item, all but attribute modifiers and item name
        ItemStack newStack = new ItemStack(nextItemOptional.get(), oldStack.getCount());
        DataComponentPatch.Builder data_component_patch$builder = DataComponentPatch.builder();
        oldStack.getComponents().forEach((component) -> { // newStack does not inherit the attribute modifiers, item name, item model and equippable (including equipment slots, equip sound and equipment entity model resource location)
            if (!component.type().equals(DataComponents.ATTRIBUTE_MODIFIERS) 
             && !component.type().equals(DataComponents.ITEM_NAME) 
             && !component.type().equals(DataComponents.ITEM_MODEL)
             && !component.type().equals(DataComponents.EQUIPPABLE)) {
                data_component_patch$builder.set((DataComponentType<Object>) component.type(), (Object) component.value());
            }
        });
        newStack.applyComponents(data_component_patch$builder.build());
        return newStack;
    }

    private static void replaceArmorItem(LivingEntity entity, ItemStack itemStack, EquipmentSlot slot) {
        // setting the new item
        if (entity instanceof Player player) {
            player.getInventory().setItem(slot.getIndex(Inventory.INVENTORY_SIZE), itemStack); // using this specifically for players can set items 'silently'; e.g. for armor without triggering equipment sound
            player.inventoryMenu.broadcastChanges();
        } else {
            entity.setItemSlot(slot, itemStack); 
        }
    }
    private static void replaceInventoryItem(Player player, ItemStack itemStack, int index) {
        player.getInventory().setItem(index, itemStack); 
        player.inventoryMenu.broadcastChanges();
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
