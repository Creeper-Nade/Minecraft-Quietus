package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.packet.ManaPack;
import com.minecraftquietus.quietus.util.handler.ClientPayloadHandler;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.minecraftquietus.quietus.Quietus.MODID;
import static com.minecraftquietus.quietus.util.QuietusAttributes.MANA_REGEN_BONUS;
import static com.minecraftquietus.quietus.util.QuietusAttributes.MAX_MANA;

@EventBusSubscriber(modid=MODID,bus = EventBusSubscriber.Bus.MOD)
public class QuietusIModBusEvent {
    @SubscribeEvent
    public static void PayloadHandlerRegistration(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToClient(ManaPack.TYPE, ManaPack.MANA_PACK_STREAM_CODEC, ClientPayloadHandler::ManaHandler);
    }


    @SubscribeEvent
    public static void modifyDefaultAttributes(EntityAttributeModificationEvent event) {
        // We can also check if a given EntityType already has a given attribute.
        // In this example, if villagers don't have the armor attribute already, we add it.
        if (!event.has(EntityType.PLAYER, MAX_MANA)) {
            event.add(EntityType.PLAYER, MAX_MANA,20);
        }
        if (!event.has(EntityType.PLAYER, MANA_REGEN_BONUS)) {
            event.add(EntityType.PLAYER, MANA_REGEN_BONUS,0);
        }
    }
}
