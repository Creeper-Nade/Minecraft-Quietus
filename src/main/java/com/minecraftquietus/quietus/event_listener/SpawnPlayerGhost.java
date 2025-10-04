package com.minecraftquietus.quietus.event_listener;

import com.minecraftquietus.quietus.entity.QuietusEntityTypes;
import com.minecraftquietus.quietus.entity.monster.PlayerGhost;
import com.minecraftquietus.quietus.util.QuietusGameRules;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.GameRules;
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
        if (event.getEntity() instanceof ServerPlayer player && player.serverLevel().getGameRules().getBoolean(QuietusGameRules.FRAGMENT_SPAWNING) && !player.serverLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {


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

            PlayerGhost ghost = new PlayerGhost(QuietusEntityTypes.PLAYER_GHOST.get(), player.level());
            ghost.setPos(player.position());
            ghost.setLoot(ghostLoot);
            ghost.setPlayerData(player);

            // Make sure the entity can be spawned
            if (player.level().addFreshEntity(ghost)) {
                LOGGER.info("Spawned PlayerGhostEntity at " + player.position());
            }
            else {
                LOGGER.error("Failed to spawn PlayerGhostEntity at " + player.position());
                // If ghost spawning fails, drop items normally
                for (ItemStack stack : ghostLoot) {
                    player.spawnAtLocation((ServerLevel) player.level(),stack);
                }
            }
        }
    }

    private static boolean isKeptSlot(int slot) {
        return (slot >= 0 && slot < 9) || // Hotbar
                (slot >= 36 && slot < 40); // Armor
    }


    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.Clone event) {
        if(event.getEntity() instanceof ServerPlayer player)
        {
            if (event.isWasDeath() && player.serverLevel().getGameRules().getBoolean(QuietusGameRules.FRAGMENT_SPAWNING)&& !player.serverLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                event.getEntity().getInventory().replaceWith(event.getOriginal().getInventory());
            }
        }

    }
}
