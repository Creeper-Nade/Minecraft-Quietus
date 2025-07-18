package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.item.QuietusComponents;
import com.minecraftquietus.quietus.item.WeatheringCopperItems;
import com.minecraftquietus.quietus.item.WeatheringIronItems;
import com.minecraftquietus.quietus.item.WeatheringItem;
import com.minecraftquietus.quietus.item.component.CanDecay;
import com.minecraftquietus.quietus.util.QuietusGameRules;
import com.minecraftquietus.quietus.util.container.ContainerUtil;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractChestBoat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.Slot;
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
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import static net.minecraft.world.level.GameRules.RULE_RANDOMTICKING;

import static com.minecraftquietus.quietus.Quietus.MODID;

@EventBusSubscriber(modid = MODID)
/**
 * WeatheringHandler is responsible for all weathering-related processes, 
 * desired to weather world items  (including from containers and entities) spontaneously over time,
 * whether oxidizing or decaying in Quietus.
 */
public class WeatheringHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Set<BaseContainerBlockEntity> LOADED_CONTAINERS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static int lastServerTick = 0;

    @SubscribeEvent
    /**
     * Does entities equipments weathering 
     * (for Player inventories, for every LivingEntity armor they are wearing and held item)
     */
    @SuppressWarnings("null")
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        Level level = entity.level();
        int serverTick = lastServerTick;
        float tick_chance = 3.0f / 4096.0f; // default value in case server is null, given 4096 blocks per chunk
        if (!level.isClientSide() && !Objects.isNull(level.getServer())) { // server side only
            MinecraftServer server = level.getServer();
            tick_chance = getRandomTickChance(server);
            serverTick = level.getServer().getTickCount();
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
                    ItemStack itemstack = armorItems[i];
                    if (itemstack.has(QuietusComponents.CAN_DECAY) && isTimeToDecay(server)) {
                        Optional<ItemStack> converted_to = itemstack.get(QuietusComponents.CAN_DECAY.get()).changeDecayAndMakeConvertedItemIfDecayed(itemstack, 1);
                        if (converted_to.isPresent()) {
                            itemstack = converted_to.get();
                            replaceArmorItem(livingEntity, itemstack, armorSlots[i]);
                        }
                    }
                    if (entity.getRandom().nextFloat() < tick_chance) {
                        if (WeatheringItem.canWeather(itemstack.getItem())) { // only if this item is weatherable, and also take in random ticking chance
                            Optional<Item> nextOptional = checkAndGetNextWeatherItem(itemstack, armorItems, level.dimensionType().ultraWarm());
                            if (nextOptional.isPresent()) {
                                //LOGGER.info(itemstack.getItem().getName().getString() + " is oxidizing to " + nextOptional.get().asItem().getName().getString());
                                replaceArmorItem(livingEntity, makeNewWeatheredStack(nextOptional, itemstack), armorSlots[i]);
                            }
                        }
                    }
                }
                if (livingEntity instanceof Player player) { // also checks for player inventory for oxidizing
                    Inventory inventory = player.getInventory();
                    for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
                        ItemStack itemstack = inventory.getItem(i);
                        if (itemstack.has(QuietusComponents.CAN_DECAY) && isTimeToDecay(server)) {
                            Optional<ItemStack> converted_to = itemstack.get(QuietusComponents.CAN_DECAY.get()).changeDecayAndMakeConvertedItemIfDecayed(itemstack, 1);
                            if (converted_to.isPresent()) {
                                itemstack = converted_to.get();
                                replaceInventoryItem(player, itemstack, i);
                            }
                        }
                        if (player.getRandom().nextFloat() < tick_chance) { 
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
                                    itemstack = makeNewWeatheredStack(nextOptional, itemstack);
                                    replaceInventoryItem(player, itemstack, i);
                                }
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
                    if (itemstack.has(QuietusComponents.CAN_DECAY) && isTimeToDecay(server)) {
                        Optional<ItemStack> converted_to = itemstack.get(QuietusComponents.CAN_DECAY.get()).changeDecayAndMakeConvertedItemIfDecayed(itemstack, 1);
                        if (converted_to.isPresent()) {
                            itemstack = converted_to.get();
                            containerEntity.setItem(i, itemstack);
                        }
                    }
                    if (entity.getRandom().nextFloat() < tick_chance) { 
                        if (WeatheringItem.canWeather(itemstack.getItem())) {
                            ItemStack[] surroundingItems = ContainerUtil.getSurroundingItems(i, 1, 1, containerEntity, perRow, false);
                            Optional<Item> nextOptional = checkAndGetNextWeatherItem(itemstack, surroundingItems, level.dimensionType().ultraWarm());
                            if (nextOptional.isPresent()) {
                                itemstack = makeNewWeatheredStack(nextOptional, itemstack);
                                containerEntity.setItem(i, makeNewWeatheredStack(nextOptional, itemstack));
                            }
                        }
                    }
                }
            } else
            /* ItemFrame */
            if (entity instanceof ItemFrame itemFrame) {
                ItemStack itemstack = itemFrame.getItem();
                if (itemstack.has(QuietusComponents.CAN_DECAY) && isTimeToDecay(server)) {
                    Optional<ItemStack> converted_to = itemstack.get(QuietusComponents.CAN_DECAY.get()).changeDecayAndMakeConvertedItemIfDecayed(itemstack, 1);
                    if (converted_to.isPresent()) {
                        itemstack = converted_to.get();
                        itemFrame.setItem(itemstack);
                    }
                }
                if (entity.getRandom().nextFloat() < tick_chance) { 
                    if (WeatheringItem.canWeather(itemstack.getItem())) {
                        ItemStack[] surroundingItems = new ItemStack[0]; // it displays just 1 item!
                        Optional<Item> nextOptional = checkAndGetNextWeatherItem(itemstack, surroundingItems, level.dimensionType().ultraWarm());
                        if (nextOptional.isPresent()) {
                            itemstack = makeNewWeatheredStack(nextOptional, itemstack);
                            itemFrame.setItem(itemstack);
                        }
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
        if (!level.isClientSide() && !Objects.isNull(level.getServer())) {
            float tick_chance = 3.0f / 4096.0f; // default value in case server is null, given 4096 blocks per chunk
            MinecraftServer server = level.getServer();
            tick_chance = getRandomTickChance(server);
            for (BaseContainerBlockEntity container : LOADED_CONTAINERS) {
                BlockEntity get_block_entity = level.getBlockEntity(container.getBlockPos());
                if (!Objects.isNull(get_block_entity) && get_block_entity.equals(container)) { // only operate this block entity if it is in this level
                    if (container.isRemoved()) {
                        LOADED_CONTAINERS.remove(container);
                        continue;
                    }
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
                        if (itemstack.has(QuietusComponents.CAN_DECAY) && isTimeToDecay(server)) {
                            Optional<ItemStack> converted_to = itemstack.get(QuietusComponents.CAN_DECAY.get()).changeDecayAndMakeConvertedItemIfDecayed(itemstack, 1);
                            if (converted_to.isPresent()) {
                                itemstack = converted_to.get();
                                container.setItem(i, itemstack);
                            }
                        }
                        if (level.getRandom().nextFloat() < tick_chance) { // accounts for randomTickSpeed gamerule
                            if (WeatheringItem.canWeather(itemstack.getItem())) {
                                ItemStack[] surroundingItems = ContainerUtil.getSurroundingItems(i, 1, 1, container, perRow, false);
                                Optional<Item> nextOptional = checkAndGetNextWeatherItem(itemstack, surroundingItems, level.dimensionType().ultraWarm());
                                if (nextOptional.isPresent()) {
                                    itemstack = makeNewWeatheredStack(nextOptional, itemstack);
                                    container.setItem(i, itemstack);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        int serverTick = server.getTickCount();
        if (isTimeToDecay(server)) {
            lastServerTick = serverTick;
        }
    }
    
    /* @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (Objects.nonNull(player)) {
            Inventory inv = player.getInventory();
            int slot = inv.getSelectedSlot();
            ItemStack itemstackor = inv.getItem(slot);
            ItemStack itemstack = itemstackor.copy();
            if (!itemstack.isEmpty() && itemstack.has(QuietusComponents.CAN_DECAY.get()) && player.isShiftKeyDown()){
                System.out.println("test");
                Optional<ItemStack> converted_to = itemstack.get(QuietusComponents.CAN_DECAY.get()).changeDecayAndMakeConvertedItemIfDecayed(itemstack, 1);
                if (converted_to.isPresent()) {
                inv.setItem(slot, converted_to.get());}
                else {
                    inv.setItem(slot, itemstack);
                }
            }
        }
    } */

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
        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (blockEntity instanceof BaseContainerBlockEntity container) {
            LOADED_CONTAINERS.add(container);
        }
    }
    @SubscribeEvent
    public static void onBlockDestruction(BlockEvent.BreakEvent event) {
        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (blockEntity instanceof BaseContainerBlockEntity container) {
            LOADED_CONTAINERS.remove(container);
        }
    }

    /**
     * Allows items otherwise identical but with different decay values to be stacked on each other
     */
    @SubscribeEvent
    public static void onItemStackedOnOther(ItemStackedOnOtherEvent event) {
        Slot targetSlot = event.getSlot();
        SlotAccess carriedSlot = event.getCarriedSlotAccess();
        ItemStack carriedItem = event.getCarriedItem();
        ItemStack targetItem = event.getStackedOnItem();
        if (carriedItem.isEmpty() || targetItem.isEmpty()) return; // do nothing if either items are empty
        /* Checking if the items are all identical but Decay */
        if (carriedItem.getItem() == targetItem.getItem() && carriedItem.has(QuietusComponents.CAN_DECAY.get()) && targetItem.has(QuietusComponents.CAN_DECAY.get())) {
            DataComponentMap carried_components = carriedItem.getComponents();
            DataComponentMap target_components = targetItem.getComponents();
            Set<DataComponentType<?>> all_component_types = new HashSet<>(carried_components.keySet());
            all_component_types.addAll(target_components.keySet());
            boolean components_identical = true;
            for (DataComponentType<?> type : all_component_types) {
                if (type == QuietusComponents.DECAY.get()) { // skip decay value comparison
                    continue;
                } else if (!Objects.equals(carriedItem.get(type), targetItem.get(type))) {
                    components_identical = false;
                    System.out.println("unidentical on: " + type.toString());
                    break;
                }
            }
            if (components_identical) {
                /* Checked are identical items but Decay, now make them merge */
                int carried_decay = carriedItem.getOrDefault(QuietusComponents.DECAY.get(), 0).intValue();
                int target_decay = targetItem.getOrDefault(QuietusComponents.DECAY.get(), 0).intValue();
                int max_stack_size = targetItem.getMaxStackSize();
                if (targetItem.getCount() != max_stack_size) { // the target item is not further stackable (count reaching its max stack)
                    int over_max_count = carriedItem.getCount() + targetItem.getCount() - max_stack_size;
                    if (over_max_count > 0) {
                        ItemStack newCarriedItem = carriedItem.copy();
                        newCarriedItem.setCount(over_max_count);
                        carriedSlot.set(newCarriedItem);
                        ItemStack newTargetItem = targetItem.copy();
                        newTargetItem.set(QuietusComponents.DECAY.get(), (int)Math.floor((carried_decay*(carriedItem.getCount()-over_max_count) + target_decay*targetItem.getCount()) / max_stack_size));
                        newTargetItem.setCount(max_stack_size);
                        targetSlot.set(newTargetItem);
                        event.setCanceled(true);
                    } else {
                        carriedSlot.set(ItemStack.EMPTY);
                        ItemStack newTargetItem = targetItem.copy();
                        newTargetItem.set(QuietusComponents.DECAY.get(), (int)Math.floor((carried_decay*carriedItem.getCount() + target_decay*targetItem.getCount()) / (carriedItem.getCount()+targetItem.getCount())));
                        newTargetItem.setCount(carriedItem.getCount()+targetItem.getCount());
                        targetSlot.set(newTargetItem);
                        event.setCanceled(true);
                    }
                }
            }
            
        }
        /* slot.set(targetItem);
        event.setCanceled(true); */
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
        ItemStack newStack = new ItemStack(nextItemOptional.get(), oldStack.getCount());
        DataComponentPatch.Builder data_component_patch$builder = DataComponentPatch.builder();
        /**
         * the weathered newStack does not inherit the follows: 
         * attribute modifiers, item name, item model 
         * and equippable (including equipment slots, equip sound and equipment entity model resource location)
         */
        oldStack.getComponents().forEach((component) -> { 
            if (!component.type().equals(DataComponents.ATTRIBUTE_MODIFIERS) 
             && !component.type().equals(DataComponents.ITEM_NAME) 
             && !component.type().equals(DataComponents.ITEM_MODEL)
             && !component.type().equals(DataComponents.EQUIPPABLE)
             && !component.type().equals(DataComponents.FOOD)
                && !component.type().equals(DataComponents.CONSUMABLE)) {
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
            entity.getSlot(slot.getIndex(100)).set(itemStack);
            entity.setItemSlot(slot, itemStack); 
        }
    }
    private static void replaceInventoryItem(Player player, ItemStack itemStack, int index) {
        player.getInventory().setItem(index, itemStack); 
        player.inventoryMenu.broadcastChanges();
    }
    private static float getRandomTickChance(MinecraftServer server) {
        return (float)server.getGameRules().getRule(RULE_RANDOMTICKING).get() / 4096.0f;
    }

    /**
     * Decay methods
     */
    private static boolean isTimeToDecay(MinecraftServer server) {
        return server.getTickCount() > (lastServerTick + server.getGameRules().getRule(QuietusGameRules.TICKS_PER_DECAY).get()); // decays once per 5 seconds (default game rule)
    }
}
