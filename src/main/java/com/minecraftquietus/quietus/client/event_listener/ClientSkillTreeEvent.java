package com.minecraftquietus.quietus.client.event_listener;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import com.minecraftquietus.quietus.client.handler.ClientSkillTreePayloadHandler;

import static com.minecraftquietus.quietus.Quietus.MODID;


@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientSkillTreeEvent {

    @SubscribeEvent
    public static void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientSkillTreePayloadHandler.initialize();
    }

    @SubscribeEvent
    public static void onClientLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSkillTreePayloadHandler.close();
    }
}