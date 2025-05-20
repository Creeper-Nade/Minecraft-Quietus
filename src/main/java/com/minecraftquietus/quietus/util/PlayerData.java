package com.minecraftquietus.quietus.util;

import com.minecraftquietus.quietus.packet.ManaPack;
import com.minecraftquietus.quietus.util.mana.ManaComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;


public class PlayerData {
    public static void ManapackToPlayer(ServerPlayer serverPlayer, ManaComponent manaComponent) {
        PacketDistributor.sendToPlayer(serverPlayer, new ManaPack(manaComponent.getMaxMana(), manaComponent.getMana()));
        //System.out.println("mana"+manaComponent.getMana());
        //System.out.println(serverPlayer);
        //System.out.println(manaComponent);
    }

    public static void ManapackToPlayer(ServerPlayer serverPlayer) {
        ManapackToPlayer(serverPlayer, serverPlayer.getData(ManaComponent.MANA_ATTACHMENT));
    }
}
