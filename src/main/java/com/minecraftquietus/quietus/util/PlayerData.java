package com.minecraftquietus.quietus.util;

import com.minecraftquietus.quietus.core.ManaComponent;
import com.minecraftquietus.quietus.packet.GhostStatePayload;
import com.minecraftquietus.quietus.packet.ManaPack;

import com.minecraftquietus.quietus.packet.PlayerReviveCooldownPack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;


public class PlayerData {
    public static void manapackToPlayer(ServerPlayer serverPlayer, ManaComponent manaComponent) {
        PacketDistributor.sendToPlayer(serverPlayer, new ManaPack(manaComponent.getMaxMana(), manaComponent.getMana(),manaComponent.getSpeedChargeStatus()));
    }

    public static void ManapackToPlayer(ServerPlayer serverPlayer) {
        manapackToPlayer(serverPlayer, serverPlayer.getData(QuietusAttachments.MANA_ATTACHMENT));
    }

    public static void GhostPackToPlayer(ServerPlayer serverPlayer, Boolean isGhost, Component deathMessage,int Max_CD,boolean hardcore) {
        PacketDistributor.sendToPlayer(serverPlayer, new GhostStatePayload(isGhost,deathMessage,Max_CD,hardcore));
    }
    public static void ReviveCDToPlayer(ServerPlayer serverPlayer, int CD) {
        PacketDistributor.sendToPlayer(serverPlayer, new PlayerReviveCooldownPack(CD));
    }
}
