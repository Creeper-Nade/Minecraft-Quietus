package com.minecraftquietus.quietus.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.minecraftquietus.quietus.Quietus;
import com.minecraftquietus.quietus.client.packet.DoDecayPacket;
import com.minecraftquietus.quietus.client.packet.GhostStatePacket;
import com.minecraftquietus.quietus.client.packet.ManaPacket;
import com.minecraftquietus.quietus.client.packet.PlayerRevivalCooldownPacket;
import com.minecraftquietus.quietus.client.packet.SkillTreeUpdatePacket;
import com.minecraftquietus.quietus.client.packet.WeatherItemContainerPacket;
import com.minecraftquietus.quietus.core.mana.ManaComponent;
import com.minecraftquietus.quietus.server.QuietusReloadableResources;
import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillPointProgress;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.ItemContainerContents;
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

    public static void sendSkillTreePackToPlayer(ServerPlayer serverPlayer) {
        Map<ResourceLocation,SkillCategory> categories = 
            Objects.nonNull(QuietusReloadableResources.getSkillCategories()) ? 
            QuietusReloadableResources.getSkillCategories() : 
            Map.of();;
        Map<ResourceLocation,SkillPointProgress.ClientData> progresses = new LinkedHashMap<>();
        if (Objects.nonNull(Quietus.playerData.getSkillTree(serverPlayer.getUUID()))) {
            Quietus.playerData.getSkillTree(serverPlayer.getUUID()).asData().forEach(
                (resourceLocation, progress) -> {
                    progresses.put(resourceLocation, progress.asClientData());
                }
            );
        }
        PacketDistributor.sendToPlayer(serverPlayer, new SkillTreeUpdatePacket(categories, progresses));
    }

}
