package com.minecraftquietus.quietus.util;

import com.minecraftquietus.quietus.core.ManaComponent;
import com.minecraftquietus.quietus.packet.ManaPack;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;


public class PlayerData {
    public static void manapackToPlayer(ServerPlayer serverPlayer, ManaComponent manaComponent) {
        PacketDistributor.sendToPlayer(serverPlayer, new ManaPack(manaComponent.getMaxMana(), manaComponent.getMana(),manaComponent.getSpeedChargeStatus()));
        //System.out.println("mana"+manaComponent.getMana());
        //System.out.println(serverPlayer);
        //System.out.println(manaComponent);
    }

    public static void ManapackToPlayer(ServerPlayer serverPlayer) {
        manapackToPlayer(serverPlayer, serverPlayer.getData(QuietusAttachments.MANA_ATTACHMENT));
    }
}
