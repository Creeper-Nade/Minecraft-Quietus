package com.minecraftquietus.quietus.util;

import com.minecraftquietus.quietus.core.ManaComponent;
import com.minecraftquietus.quietus.packet.GhostStatePacket;
import com.minecraftquietus.quietus.packet.ManaPacket;

import com.minecraftquietus.quietus.packet.PlayerRevivalCooldownPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;


public class PlayerData {
    public static void manapackToPlayer(ServerPlayer serverPlayer, ManaComponent manaComponent) {
        PacketDistributor.sendToPlayer(serverPlayer, new ManaPacket(manaComponent.getMaxMana(), manaComponent.getMana(),manaComponent.getSpeedChargeStatus()));
    }

    public static void manaPackToPlayer(ServerPlayer serverPlayer) {
        manapackToPlayer(serverPlayer, serverPlayer.getData(QuietusAttachments.MANA_ATTACHMENT));
    }

    public static void ghostPackToPlayer(ServerPlayer serverPlayer, Boolean isGhost, Component deathMessage,int Max_CD,boolean hardcore) {
        PacketDistributor.sendToPlayer(serverPlayer, new GhostStatePacket(isGhost,deathMessage,Max_CD,hardcore));
    }
    public static void revivalCDToPlayer(ServerPlayer serverPlayer, int CD) {
        PacketDistributor.sendToPlayer(serverPlayer, new PlayerRevivalCooldownPacket(CD));
    }
}
