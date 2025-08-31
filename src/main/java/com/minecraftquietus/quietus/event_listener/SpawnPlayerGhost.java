package com.minecraftquietus.quietus.event_listener;

import com.minecraftquietus.quietus.entity.QuietusEntityTypes;
import com.minecraftquietus.quietus.entity.monster.PlayerGhost;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.minecraftquietus.quietus.Quietus.MODID;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.GAME)
public class SpawnPlayerGhost {
    private static final Logger LOGGER = LogUtils.getLogger();
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Don't cancel the event entirely, just prevent default dropping
            // event.setCanceled(true); // Remove this line

            // Store hotbar/armor items to restore later
            List<ItemStack> keptItems = new ArrayList<>();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack slotItem = player.getItemBySlot(slot);
                if (!slotItem.isEmpty()) {
                    keptItems.add(slotItem.copy());
                }
            }

            // Save the kept items to player's persistent data
            CompoundTag data = player.getPersistentData();
            ListTag keptItemsTag = new ListTag();
            HolderLookup.Provider provider = player.level().registryAccess();

            for (ItemStack stack : keptItems) {
                // Skip empty stacks to avoid serialization errors:cite[1]
                if (!stack.isEmpty()) {
                    CompoundTag stackTag = new CompoundTag();
                    stack.save(provider, stackTag);
                    keptItemsTag.add(stackTag);
                }
            }
            data.put("kept_items", keptItemsTag);

            // Collect loot (excluding hotbar/armor)
            List<ItemStack> ghostLoot = new ArrayList<>();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (!isKeptSlot(i)) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (!stack.isEmpty()) {
                        ghostLoot.add(stack.copy());
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                    }
                }
            }

            // Spawn ghost on the server side only
            if (!player.level().isClientSide) {
                PlayerGhost ghost = new PlayerGhost(QuietusEntityTypes.PLAYER_GHOST.get(), player.level());
                ghost.setPos(player.position());
                ghost.setLoot(ghostLoot);
                ghost.setPlayerHeadTexture(player);

                // Make sure the entity can be spawned
                if (player.level().addFreshEntity(ghost)) {
                    LOGGER.info("Spawned PlayerGhostEntity at " + player.position());
                } else {
                    LOGGER.error("Failed to spawn PlayerGhostEntity at " + player.position());
                    // If ghost spawning fails, drop items normally
                    for (ItemStack stack : ghostLoot) {
                        player.spawnAtLocation((ServerLevel) player.level(),stack);
                    }
                }
            }
        }
    }

    private static boolean isKeptSlot(int slot) {
        return (slot >= 0 && slot < 9) || // Hotbar
                (slot >= 36 && slot < 40); // Armor
    }

    // Helper method to load item list from NBT
    private static List<ItemStack> loadItemList(HolderLookup.Provider provider, ListTag listTag) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag itemTag = listTag.getCompoundOrEmpty(i);
            Optional<ItemStack> stack = ItemStack.parse(provider, itemTag);
            stack.ifPresent(items::add);
        }
        return items;
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player newPlayer = event.getEntity();
            CompoundTag data = newPlayer.getPersistentData();
            if (data.contains("kept_items")) {
                ListTag keptItemsTag = data.getListOrEmpty("kept_items");
                HolderLookup.Provider provider = newPlayer.level().registryAccess();
                List<ItemStack> keptItems = loadItemList(provider, keptItemsTag);

                // Restore items to their proper slots
                EquipmentSlot[] slots = EquipmentSlot.values();
                for (int i = 0; i < keptItems.size() && i < slots.length; i++) {
                    newPlayer.setItemSlot(slots[i], keptItems.get(i));
                }

                // Clear the saved items
                data.remove("kept_items");
            }
        }
    }
}
