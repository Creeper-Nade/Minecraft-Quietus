package com.quietus.util;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.quietus.Quietus;
import com.quietus.client.packet.DoDecayPacket;
import com.quietus.client.packet.GhostStatePacket;
import com.quietus.client.packet.GrapplingActiveHookPacket;
import com.quietus.client.packet.GrapplingHookPhysicsPacket;
import com.quietus.client.packet.ManaPacket;
import com.quietus.client.packet.PlayerRevivalCooldownPacket;
import com.quietus.client.packet.SkillTreeAdvancementsGrantRevokePacket;
import com.quietus.client.packet.SkillTreeAdvancementsUpdatePacket;
import com.quietus.client.packet.SkillTreeUpdatePacket;
import com.quietus.client.packet.WeatherItemContainerPacket;
import com.quietus.core.mana.ManaComponent;
import com.quietus.server.PlayerSkillTree;
import com.quietus.server.QuietusReloadableResources;
import com.quietus.skilltree.Prerequisites;
import com.quietus.skilltree.SkillCategory;
import com.quietus.skilltree.SkillPoint;
import com.quietus.skilltree.SkillPointProgress;
import com.quietus.skilltree.SkillTreeNode;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;


public class PlayerClientPacketDistributor {
    public static void sendManaPackToPlayer(ServerPlayer serverPlayer, ManaComponent manaComponent) {
        PacketDistributor.sendToPlayer(serverPlayer, new ManaPacket(manaComponent.getMaxMana(), manaComponent.getMana(),manaComponent.getSpeedChargeStatus()));
    }

    public static void sendManaPackToPlayer(ServerPlayer serverPlayer) {
        sendManaPackToPlayer(serverPlayer, serverPlayer.getData(QuietusAttachments.MANA_ATTACHMENT));
    }

    public static void sendGhostPackToPlayer(ServerPlayer serverPlayer, Boolean isGhost, Component deathMessage,int Max_CD,boolean hardcore) {
        PacketDistributor.sendToPlayer(serverPlayer, new GhostStatePacket(isGhost,deathMessage,Max_CD,hardcore));
    }
    public static void sendRevivalCDToPlayer(ServerPlayer serverPlayer, int CD) {
        PacketDistributor.sendToPlayer(serverPlayer, new PlayerRevivalCooldownPacket(CD));
    }

    public static void sendPackToDecayItemFromSlotOfEntity(Entity entity, EquipmentSlot slot, int amount) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new DoDecayPacket(entity.getId(), slot, amount));
    }
    public static void sendPackToWeatherItemContainerFromSlotOfEntity(Entity entity, EquipmentSlot slot, ItemContainerContents containerContents) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new WeatherItemContainerPacket(entity.getId(), slot, containerContents));
    }

    public static void sendGrapplePhysicsPackToEntity(ServerPlayer serverPlayer, Vec3 velocity) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, GrapplingHookPhysicsPacket.fromVelocity(velocity));
    }
    public static void sendGrappleActivityPackToEntity(ServerPlayer serverPlayer, Boolean active,int id) {
        PacketDistributor.sendToPlayer(serverPlayer,new GrapplingActiveHookPacket(active,id));
    }

    /**
     * Make a client-bound skill tree packet for player
     * skill tree GUI. This skill tree filters out all
     * skill tree nodes that should not be visible
     * to the player, including: nodes without
     * DisplayInfo, nodes having unmet layout
     * prerequisites, and all the children and indirect
     * children of mentioned nodes.
     * @param player
     * @return
     */
    public static SkillTreeUpdatePacket makeClientboundSkillTreePack(ServerPlayer player) {
        Map<Identifier, SkillCategory> originalCategories =
            Objects.nonNull(QuietusReloadableResources.getSkillCategories()) ?
                QuietusReloadableResources.getSkillCategories() :
                Map.of();

        Set<Identifier> completedParents = new HashSet<>();
        PlayerSkillTree playerSkillTree = Objects.nonNull(Quietus.playerData) ? Quietus.playerData.getSkillTree(player.getUUID()) : null;
        if (Objects.nonNull(playerSkillTree)) {
            playerSkillTree.getProgresses().forEach((node, progress) -> {
                if (progress.isProgressing()) {
                    completedParents.add(node.getId());
                }
            });
        }

        PlayerAdvancements playerAdvancements = player.getAdvancements();
        ServerAdvancementManager advancementTree = player.level().getServer().getAdvancements();

        Set<SkillTreeNode> filteredOut = new HashSet<>();
        Queue<SkillTreeNode> queue = new ArrayDeque<>();

        for (SkillCategory category : originalCategories.values()) { // filtering out nodes with no display or not met layout prerequisites
            for (SkillTreeNode node : category.getNodesMap().values()) {
                SkillPoint skillPoint = node.getSkillPoint();
                if (skillPoint.display().isEmpty() || !isLayoutPrerequisitesMet(skillPoint.layout().prerequisites(), playerAdvancements, advancementTree, completedParents)) {
                    if (filteredOut.add(node)) {
                        queue.add(node);
                    }
                }
            }
        }

        while (!queue.isEmpty()) { // loop and filtering out the children of filtered out nodes until reaching leaf nodes
            SkillTreeNode current = queue.poll();
            for (SkillTreeNode child : current.children()) {
                if (filteredOut.add(child)) {
                    queue.add(child);
                }
            }
        }

        Set<Identifier> filteredOutIds = filteredOut.stream()
            .map(SkillTreeNode::getId)
            .collect(Collectors.toSet());

        Map<Identifier, SkillCategory> filteredCategories = new LinkedHashMap<>();
        originalCategories.forEach((catId, category) -> {
            Map<Identifier, SkillPoint> filteredNodesMap = new HashMap<>();
            category.getNodesMap().forEach((nodeId, node) -> {
                if (!filteredOutIds.contains(nodeId)) {
                    filteredNodesMap.put(nodeId, node.getSkillPoint());
                }
            });
            SkillCategory newCategory = new SkillCategory(
                category.getId(),
                category.maxWidth(),
                category.seed(),
                category.prerequisites(),
                category.display()
            );
            newCategory.addAll(filteredNodesMap);
            filteredCategories.put(catId, newCategory);
        });

        Map<Identifier, SkillPointProgress.ClientData> progresses = new LinkedHashMap<>();
        if (Objects.nonNull(playerSkillTree)) {
            playerSkillTree.asData().forEach((nodeId, progress) -> {
                if (!filteredOutIds.contains(nodeId)) {
                    progresses.put(nodeId, progress.asClientData());
                }
            });
        }

        return new SkillTreeUpdatePacket(filteredCategories, progresses);
    }

    private static boolean isLayoutPrerequisitesMet(Prerequisites prereq, PlayerAdvancements playerAdvancements, ServerAdvancementManager advancementTree, Set<Identifier> completedParents) {
        if (prereq.requirements().isEmpty()) {
            return true;
        }
        Set<Identifier> completedAdvancements = new HashSet<>();
        for (Identifier advId : prereq.advancements().values()) {
            AdvancementHolder holder = advancementTree.get(advId);
            if (Objects.nonNull(holder) && playerAdvancements.getOrStartProgress(holder).isDone()) {
                completedAdvancements.add(advId);
            }
        }
        Prerequisites.CompletionStatus status = Prerequisites.CompletionStatus.make(prereq, completedAdvancements, completedParents);
        return prereq.requirements().test(status);
    }
    public static void sendSkillTreePackToPlayer(ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, makeClientboundSkillTreePack(serverPlayer));
    }
    public static void sendPackToGrantSkillTreeAdvancementToPlayer(ServerPlayer serverPlayer, Identifier advancementId) {
        PacketDistributor.sendToPlayer(serverPlayer, new SkillTreeAdvancementsGrantRevokePacket(advancementId, true));
    }
    public static void sendPackToRevokeSkillTreeAdvancementToPlayer(ServerPlayer serverPlayer, Identifier advancementId) {
        PacketDistributor.sendToPlayer(serverPlayer, new SkillTreeAdvancementsGrantRevokePacket(advancementId, false));
    }
    public static void sendSkillTreeAdvancementSyncPackToPlayer(ServerPlayer serverPlayer, Set<Identifier> advancementIds) {
        PacketDistributor.sendToPlayer(serverPlayer, new SkillTreeAdvancementsUpdatePacket(advancementIds));
    }

    

}
