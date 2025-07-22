package com.minecraftquietus.quietus.event_listener;

import com.minecraftquietus.quietus.client.handler.ClientPayloadHandler;
import com.minecraftquietus.quietus.client.item.DecayBarDecorator;
import com.minecraftquietus.quietus.client.model.projectile.magic.AmethystProjectileModel;
import com.minecraftquietus.quietus.packet.DoDecayPacket;
import com.minecraftquietus.quietus.packet.GhostStatePacket;
import com.minecraftquietus.quietus.packet.ManaPacket;
import com.minecraftquietus.quietus.packet.PlayerRevivalCooldownPacket;
import com.minecraftquietus.quietus.packet.WeatherItemContainerPacket;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.minecraftquietus.quietus.Quietus.MODID;
import static com.minecraftquietus.quietus.util.QuietusAttributes.MANA_REGEN_BONUS;
import static com.minecraftquietus.quietus.util.QuietusAttributes.MAX_MANA;

import com.minecraftquietus.quietus.client.renderer.BowslingerRenderer;
import com.minecraftquietus.quietus.client.renderer.ParabolerRenderer;
import com.minecraftquietus.quietus.entity.QuietusEntityTypes;
import com.minecraftquietus.quietus.entity.monster.Bowslinger;
import com.minecraftquietus.quietus.entity.monster.Paraboler;
import com.minecraftquietus.quietus.item.QuietusComponents;
import com.minecraftquietus.quietus.item.QuietusItems;
import com.minecraftquietus.quietus.item.component.CanDecay;


@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
public class QuietusIModBusEvent {
    @SubscribeEvent
    public static void PayloadHandlerRegistration(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToClient(
            ManaPacket.TYPE, 
            ManaPacket.STREAM_CODEC, 
            ClientPayloadHandler::handleMana);
        registrar.playToClient(
            GhostStatePacket.TYPE,
            GhostStatePacket.STREAM_CODEC,
            ClientPayloadHandler::handleGhostState
        );
        registrar.playToClient(
            PlayerRevivalCooldownPacket.TYPE,
            PlayerRevivalCooldownPacket.STREAM_CODEC,
            ClientPayloadHandler::handleReviveCD
        );
        registrar.playToClient(
            DoDecayPacket.TYPE,
            DoDecayPacket.STREAM_CODEC,
            ClientPayloadHandler::handleDoDecay
        );
        registrar.playToClient(
            WeatherItemContainerPacket.TYPE,
            WeatherItemContainerPacket.STREAM_CODEC,
            ClientPayloadHandler::handleWeatherItemContainer
        );
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AmethystProjectileModel.LAYER_LOCATION, AmethystProjectileModel::createBodyLayer);
    }


    @SubscribeEvent
    public static void registerDecorators(RegisterItemDecorationsEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            event.register(item, DecayBarDecorator.INSTANCE);
        }
    }



    @SubscribeEvent
    public static void modifyDefaultAttributes(EntityAttributeModificationEvent event) {
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

    @SubscribeEvent 
    /**
     * Change potion max stack size to 16.
     */
    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        event.modify(Items.POTION, builder ->    // suspicious stew
            builder.set(DataComponents.MAX_STACK_SIZE, 16)
        );
    }
    
}
