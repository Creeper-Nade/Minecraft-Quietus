package com.minecraftquietus.quietus.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import static com.minecraftquietus.quietus.Quietus.MODID;

import com.minecraftquietus.quietus.util.PlayerData;

@EventBusSubscriber(modid=MODID)
public class ManaHandler {

    @SubscribeEvent
    public static void OnLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            //System.out.println(serverPlayer);
            PlayerData.ManapackToPlayer(serverPlayer);
        }
    }
}
