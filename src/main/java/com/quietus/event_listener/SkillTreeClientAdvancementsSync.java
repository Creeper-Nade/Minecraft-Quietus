package com.quietus.event_listener;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import com.quietus.server.QuietusReloadableResources;
import com.quietus.util.PlayerClientPacketDistributor;
import com.mojang.logging.LogUtils;

import static com.quietus.Quietus.MODID;

import org.slf4j.Logger;

@EventBusSubscriber(modid = MODID)
public class SkillTreeClientAdvancementsSync {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Send all the player's advancements resource locations that are 
     * required by skill tree to the player client side
     * @param event
     */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (QuietusReloadableResources.isOpen()) {
            Player player = event.getEntity();
            Set<Identifier> requiredAdvancements = QuietusReloadableResources.getRequiredAdvancements();
            if (player instanceof ServerPlayer serverPlayer) {
                PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
                ServerAdvancementManager advancementTree = serverPlayer.level().getServer().getAdvancements();

                Set<Identifier> completedRequired = requiredAdvancements.stream()
                    .filter(id -> {
                        AdvancementHolder holder = advancementTree.get(id);
                        if (Objects.nonNull(holder)) {
                            AdvancementProgress progress = playerAdvancements.getOrStartProgress(holder);
                            return progress.isDone();
                        }
                        return false;
                    })
                    .collect(Collectors.toSet());

                PlayerClientPacketDistributor.sendSkillTreeAdvancementSyncPackToPlayer(serverPlayer, completedRequired);
            }
        }
    }

    /**
     * Syncs as player gains / gets revoked advancements. This is for the 
     * client side prerequisite GUI conditions rendering logic. 
     * @param event
     */
    @SubscribeEvent
    public static void onAdvancementProgress(AdvancementEvent.AdvancementProgressEvent event) {
        if (QuietusReloadableResources.isOpen()) {
            Player player = event.getEntity();
            Set<Identifier> requiredAdvancements = QuietusReloadableResources.getRequiredAdvancements();
            Identifier id = event.getAdvancement().id();
            if (player instanceof ServerPlayer serverPlayer) {
                if (Objects.nonNull(requiredAdvancements) && requiredAdvancements.contains(id)) {
                    switch (event.getProgressType()) {
                        case AdvancementEvent.AdvancementProgressEvent.ProgressType.GRANT:
                            PlayerClientPacketDistributor.sendPackToGrantSkillTreeAdvancementToPlayer(serverPlayer, id);
                            break;
                        case AdvancementEvent.AdvancementProgressEvent.ProgressType.REVOKE:
                            PlayerClientPacketDistributor.sendPackToRevokeSkillTreeAdvancementToPlayer(serverPlayer, id);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

}
