package com.minecraftquietus.quietus.util;

import com.minecraftquietus.quietus.core.mana.ManaComponent;
import com.minecraftquietus.quietus.packet.*;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;


public class PlayerData {
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
    public static void sendGrappleJumpPackToServer() {
        PacketDistributor.sendToServer(new GrapplingJumpReleasePacket());
    }
    public static void sendGrappleActivityPackToEntity(LivingEntity entity, Boolean active) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity,new GrapplingActiveHookPacket(active));
    }
}
