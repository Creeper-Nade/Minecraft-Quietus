package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.packet.ManaPack;
import com.minecraftquietus.quietus.util.handler.ClientPayloadHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.minecraftquietus.quietus.Quietus.MODID;

@EventBusSubscriber(modid=MODID,bus = EventBusSubscriber.Bus.MOD)
public class QuietusIModBusEvent {
    @SubscribeEvent
    public static void PayloadHandlerRegistration(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToClient(ManaPack.TYPE, ManaPack.MANA_PACK_STREAM_CODEC, ClientPayloadHandler::ManaHandler);
    }
}
