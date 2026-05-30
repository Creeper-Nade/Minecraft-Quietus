package com.quietus.event_listener;

import com.quietus.entity.QuietusEntityTypes;
import com.quietus.entity.monster.PlayerFragment;
import com.quietus.util.QuietusGameRules;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.quietus.Quietus.MODID;

@EventBusSubscriber(modid = MODID)
public class SpawnPlayerFragment {
    private static final Logger LOGGER = LogUtils.getLogger();
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level().getGameRules().get(QuietusGameRules.FRAGMENT_SPAWNING) && !player.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {


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
            if(ghostLoot.isEmpty() && player.totalExperience==0)
            {
                return;
            }
            PlayerFragment ghost = new PlayerFragment(QuietusEntityTypes.PLAYER_FRAGMENT.get(), player.level());
            ghost.setPos(player.position());
            ghost.setLoot(ghostLoot);
            ghost.setPlayerData(player);

            // Make sure the entity can be spawned
            if (player.level().addFreshEntity(ghost)) {
                LOGGER.info("Spawned PlayerFragmentEntity at " + player.position());
            }
            else {
                LOGGER.error("Failed to spawn PlayerFragmentEntity at " + player.position());
                // If ghost spawning fails, drop items normally
                for (ItemStack stack : ghostLoot) {
                    player.spawnAtLocation((ServerLevel) player.level(),stack);
                }
            }
        }
    }

    private static boolean isKeptSlot(int slot) {
        return (slot >= 0 && slot < 9) || // Hotbar
                (slot >= 36 && slot <= 40); // Armor
    }


    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.Clone event) {
        if(event.getEntity() instanceof ServerPlayer player)
        {
            if (event.isWasDeath() && player.level().getGameRules().get(QuietusGameRules.FRAGMENT_SPAWNING)&& !player.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
                event.getEntity().getInventory().replaceWith(event.getOriginal().getInventory());
            }
        }

    }
}
