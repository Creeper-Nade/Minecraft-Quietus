package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.client.model.projectile.magic.AmethystProjectileModel;
import com.minecraftquietus.quietus.packet.ManaPack;
import com.minecraftquietus.quietus.util.handler.ClientPayloadHandler;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.minecraftquietus.quietus.Quietus.MODID;
import static com.minecraftquietus.quietus.util.QuietusAttributes.MANA_REGEN_BONUS;
import static com.minecraftquietus.quietus.util.QuietusAttributes.MAX_MANA;

import javax.swing.text.html.parser.Entity;

import com.minecraftquietus.quietus.client.renderer.BowslingerRenderer;
import com.minecraftquietus.quietus.client.renderer.ParabolerRenderer;
import com.minecraftquietus.quietus.entity.QuietusEntityTypes;
import com.minecraftquietus.quietus.entity.monster.Bowslinger;
import com.minecraftquietus.quietus.entity.monster.Paraboler;

@EventBusSubscriber(modid=MODID,bus = EventBusSubscriber.Bus.MOD)
public class QuietusIModBusEvent {
    @SubscribeEvent
    public static void PayloadHandlerRegistration(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToClient(ManaPack.TYPE, ManaPack.MANA_PACK_STREAM_CODEC, ClientPayloadHandler::ManaHandler);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AmethystProjectileModel.LAYER_LOCATION, AmethystProjectileModel::createBodyLayer);
    }


    @SubscribeEvent
    public static void modifyDefaultAttributes(EntityAttributeModificationEvent event) {
        // We can also check if a given EntityType already has a given attribute.
        // In this example, if villagers don't have the armor attribute already, we add it.\
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()){
            if (!event.has(entityType, MAX_MANA)) {
                event.add(entityType, MAX_MANA,20);
            }
            if (!event.has(entityType, MANA_REGEN_BONUS)) {
                event.add(entityType, MANA_REGEN_BONUS,0);
            }
        }

    }
    @SubscribeEvent
    public static void initiateAttributes(EntityAttributeCreationEvent event) {
        event.put(QuietusEntityTypes.BOWSLINGER.get(), Bowslinger.createAttributes().build());
        event.put(QuietusEntityTypes.PARABOLER.get(), Paraboler.createAttributes().build());
    }
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(QuietusEntityTypes.BOWSLINGER.get(), BowslingerRenderer::new);
        event.registerEntityRenderer(QuietusEntityTypes.PARABOLER.get(), ParabolerRenderer::new);
    }
}
