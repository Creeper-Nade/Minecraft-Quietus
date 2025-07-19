package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.item.QuietusComponents;
import com.minecraftquietus.quietus.item.WeatheringCopperItems;
import com.minecraftquietus.quietus.item.WeatheringIronItems;
import com.minecraftquietus.quietus.item.WeatheringItem;
import com.minecraftquietus.quietus.item.component.CanDecay;
import com.minecraftquietus.quietus.util.ContainerUtil;
import com.minecraftquietus.quietus.util.ItemStackUtil;
import com.minecraftquietus.quietus.util.PlayerData;
import com.minecraftquietus.quietus.util.QuietusGameRules;
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
import net.minecraft.client.telemetry.events.WorldUnloadEvent;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.TestEnvironmentDefinition.Weather;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractChestBoat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
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
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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

    /**
     * Used by doWeatherItem() to return a result including the weathered item stack,
     * and further data related to certain changes to the weathered item.
     * Intended for implementations of doWeatherItem() to determine whether or not to update certain data.
     * @param itemStack the ItemStack after weathering (and its contents weathering if it has any)
     * @param isItemChanged whether or not is the {@link ItemStack#getItem()} changed
     * @param hasDecayed the item has undergone a decay
     * @param changedContainerContents empty if the item does not have this property, or it is not changed. Else, is the contents after weathering.
     */
    record WeatheringResult(
        ItemStack itemStack,
        boolean isItemChanged,
        boolean hasDecayed,
        Optional<ItemContainerContents> changedContainerContents
    ) {
        ItemStack get() {
            return this.itemStack;
        }

        static WeatheringResult of(ItemStack item, boolean itemChanged, boolean hasDecayed) {
            return new WeatheringResult(item, itemChanged, hasDecayed, Optional.empty());
        }
        static WeatheringResult of(ItemStack item, boolean itemChanged, boolean hasDecayed, Optional<ItemContainerContents> containerContents) {
            return new WeatheringResult(item, itemChanged, hasDecayed, containerContents);
        }
    }

    /**
     * Does weathering on the provided item stack.
     * @param stack the item stack to weather
     * @param surroundingItems surrounding item stacks taken in account for weathering check
     * @param random random source
     * @param level the level of weathering occurence
     * @param server the server of weathering occurence
     * @return WeatheringResult record
     */
    public static WeatheringResult doWeatherItem(ItemStack stack, ItemStack[] surroundingItems, RandomSource random, Level level, MinecraftServer server) {
        boolean hasChangedItem = false;
        boolean hasDecayed = false;
        ItemStack itemstack = stack;
        float tick_chance = getRandomTickChance(server);
        if (itemstack.has(QuietusComponents.CAN_DECAY) && isTimeToDecay(server)) {
            Optional<ItemStack> converted_to = itemstack.get(QuietusComponents.CAN_DECAY.get()).changeDecayAndMakeConvertedItemIfDecayed(itemstack, 1);
            hasDecayed = true;
            if (converted_to.isPresent()) {
                itemstack = converted_to.get();
                hasChangedItem = true;
            }
        }
        if (random.nextFloat() < tick_chance) {
            if (WeatheringItem.canWeather(itemstack.getItem())) { // only if this item is weatherable, and also take in random ticking chance
                Optional<Item> next_optional = checkAndGetNextWeatherItem(itemstack, surroundingItems, level.dimensionType().ultraWarm());
                if (next_optional.isPresent()) {
                    itemstack = makeNewWeatheredStack(next_optional, itemstack);
                    hasChangedItem = true;
                    //LOGGER.info(itemstack.getItem().getName().getString() + " is oxidizing to " + nextOptional.get().asItem().getName().getString());
                }
            }
        }
        if (itemstack.has(DataComponents.CONTAINER)) { // additional weathering if item has container, after checking components
            Optional<ItemContainerContents> weathered_contents = makeDoWeatherItemContainerContent(itemstack.get(DataComponents.CONTAINER), 9, random, level, server);
            if (weathered_contents.isPresent())
                itemstack.set(DataComponents.CONTAINER, weathered_contents.get());
                return WeatheringResult.of(itemstack, hasChangedItem, hasDecayed, weathered_contents);
        }
        return WeatheringResult.of(itemstack, hasChangedItem, hasDecayed);
    }

    public static Optional<ItemContainerContents> makeDoWeatherItemContainerContent(ItemContainerContents containerContents, int perRow, RandomSource random, Level level, MinecraftServer server) {
        boolean hasChanged = false;
        List<ItemStack> itemsList = new ArrayList<>(containerContents.getSlots());
        for (int i = 0; i < containerContents.getSlots(); i++) {
            ItemStack itemstack = containerContents.getStackInSlot(i); // ItemContainerContents#getStackInSlot returns a copy of the ItemStack 
            ItemStack[] surroundingItems = ContainerUtil.getSurroundingItems(i, 1, 1, containerContents, perRow, false);
            WeatheringResult result = doWeatherItem(itemstack, surroundingItems, random, level, server); // may result in endless loop: doWeatherItem calls for this method to weather contents in item container
            if (result.isItemChanged()) {
                itemsList.add(result.itemStack());
                hasChanged = true;
            } else 
                itemsList.add(itemstack);
                if (result.hasDecayed() || result.changedContainerContents().isPresent())
                    hasChanged = true;
        }
        return hasChanged ? 
            Optional.of(ItemContainerContents.fromItems(itemsList)) 
            : Optional.empty();
    }

    @SubscribeEvent
    @SuppressWarnings("null")
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        Level level = entity.level();
        if (!level.isClientSide() && !Objects.isNull(level.getServer())) { // server side only
            MinecraftServer server = level.getServer();
            /* LivingEntity */
            if (entity instanceof LivingEntity livingEntity) {
                /** Armor oxidation */
                ItemStack[] armorItems = { 
                    livingEntity.getItemBySlot(EquipmentSlot.FEET),
                    livingEntity.getItemBySlot(EquipmentSlot.LEGS),
                    livingEntity.getItemBySlot(EquipmentSlot.CHEST),
                    livingEntity.getItemBySlot(EquipmentSlot.HEAD),
                    livingEntity.getItemBySlot(EquipmentSlot.MAINHAND),
                    livingEntity.getItemBySlot(EquipmentSlot.OFFHAND),
                }; EquipmentSlot[] slots = {EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD,EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND};
                for (int i = 0; i < armorItems.length; ++i) {
                    ItemStack itemstack = armorItems[i];
                    //if (itemstack.has(QuietusComponents.CAN_DECAY) && isTimeToDecay(server)) PlayerData.sendPackToDecayItemFromSlotOfEntity(livingEntity, slots[i], 1);
                    WeatheringResult result = doWeatherItem(itemstack, armorItems, livingEntity.getRandom(), level, server);
                    if (result.isItemChanged()) replaceItem(livingEntity, result.get(), slots[i]);
                    else {
                        if (result.hasDecayed()) PlayerData.sendPackToDecayItemFromSlotOfEntity(livingEntity, slots[i], 1);
                        if (result.changedContainerContents().isPresent()) PlayerData.sendPackToWeatherItemContainerFromSlotOfEntity(livingEntity, slots[i], result.changedContainerContents.get());
                    }
                }
                if (livingEntity instanceof Player player) { // also checks for player inventory for oxidizing
                    Inventory inventory = player.getInventory();
                    for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
                        if (i == inventory.getSelectedSlot()) continue; // mainhand is already weathered via above EquipmentSlot.MAINHAND
                        ItemStack itemstack = inventory.getItem(i);
                        /* Special surroundingItems formula for inventory. Separating hotbar and other slots */
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
                        WeatheringResult result = doWeatherItem(itemstack, surroundingItems.toArray(new ItemStack[0]), player.getRandom(), level, server);
                        if (result.isItemChanged()) replaceInventoryItem(player, result.get(), i);
                    }
                }
            } else
            /* ContainerEntity (e.g. ChestBoat, MinecartChest) */
            if (entity instanceof ContainerEntity containerEntity) {
                int perRow = 9;
                int containerSize = containerEntity.getContainerSize();
                for (int i = 0; i < containerSize; i++) {
                    ItemStack itemstack = containerEntity.getItem(i);
                    WeatheringResult result = doWeatherItem(itemstack, ContainerUtil.getSurroundingItems(i, 1, 1, containerEntity, perRow, false), entity.getRandom(), level, server);
                    if (result.isItemChanged()) containerEntity.setItem(i, result.get());
                }
            } else
            /* ItemFrame */
            if (entity instanceof ItemFrame itemFrame) {
                ItemStack itemstack = itemFrame.getItem();
                if (itemstack.has(QuietusComponents.CAN_DECAY) && isTimeToDecay(server)) PlayerData.sendPackToDecayItemFromSlotOfEntity(entity, EquipmentSlot.MAINHAND, 1);
                WeatheringResult result = doWeatherItem(itemstack, new ItemStack[0], entity.getRandom(), level, server);
                if (result.isItemChanged()) itemFrame.setItem(result.get());
            } else 
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack itemstack = itemEntity.getItem();
                WeatheringResult result = doWeatherItem(itemstack, new ItemStack[0], entity.getRandom(), level, server);
                if (result.isItemChanged()) itemEntity.setItem(result.get());
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
                    || container.getType() == BlockEntityType.BARREL
                    || container.getType() == BlockEntityType.SHULKER_BOX) {
                        perRow = 9;
                    } else 
                    /* Furnaces have special logic: if lit, input item does not weather */
                    if (container instanceof AbstractFurnaceBlockEntity furnaceBlockEntity) {
                        perRow = -1;
                        int furnaceSize = furnaceBlockEntity.getContainerSize();
                        if (furnaceBlockEntity.getBlockState().getValue(AbstractFurnaceBlock.LIT)) { // is lit
                            for (int i = 1; i < furnaceSize; i ++) { // vanilla furnaces have fuel on slot 1, result on slot 2.
                                ItemStack itemstack = furnaceBlockEntity.getItem(i);
                                WeatheringResult result = doWeatherItem(itemstack, ContainerUtil.getSurroundingItems(i, 1, 1, furnaceBlockEntity, perRow, false), level.getRandom(), level, server);
                                if (result.isItemChanged()) furnaceBlockEntity.setItem(i, result.get());
                            }
                            continue; // skip following generic logic
                        }
                    } else {
                        continue;
                    }
                    int containerSize = container.getContainerSize();
                    for (int i = 0; i < containerSize; i++) {
                        ItemStack itemstack = container.getItem(i);
                        WeatheringResult result = doWeatherItem(itemstack, ContainerUtil.getSurroundingItems(i, 1, 1, container, perRow, false), level.getRandom(), level, server);
                        if (result.isItemChanged()) container.setItem(i, result.get());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        lastServerTick = event.getServer().getTickCount();
    }

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        int serverTick = server.getTickCount();
        if (isTimeToDecay(server)) {
            lastServerTick = serverTick;
        }
    }
    
    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (Objects.nonNull(player)) {
            /* Inventory inv = player.getInventory();
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
            } */
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
            /* Checked are identical items but Decay, now make them merge */
            if (ItemStackUtil.isSameItemSameComponentsExceptDecay(carriedItem, targetItem)) {
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

    private static void replaceItem(LivingEntity entity, ItemStack itemStack, EquipmentSlot slot) {
        // setting the new item
        if (entity instanceof Player player) {
            if (slot.isArmor() && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
                // ATTENTION: INVENTORY_SIZE by 1.21.5 vanilla is numerically equal to 36, and equipments indexes start from 36.
                player.getInventory().setItem(slot.getIndex(Inventory.INVENTORY_SIZE), itemStack); // using this specifically for players can set items 'silently'; e.g. for armor without triggering equipment sound
            else
                player.setItemSlot(slot, itemStack);
            player.inventoryMenu.broadcastChanges();
        } else {
            if (slot.isArmor() && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
                entity.getSlot(slot.getIndex(100)).set(itemStack); 
            else 
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
