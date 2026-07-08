package com.quietus.event_listener;

import com.quietus.client.handler.ClientPayloadHandler;
import com.quietus.client.handler.ClientSkillTreePayloadHandler;
import com.quietus.client.model.mob.PlayerFragmentRenderer;
import com.quietus.client.model.projectile.magic.AmethystProjectileModel;
import com.quietus.client.model.projectile.magic.AmethystProjectileSmallModel;
import com.quietus.client.model.projectile.misc.ChainHookModel;
import com.quietus.client.particle.DustExplosionParticle;
import com.quietus.client.particle.DustImplosionParticle;
import com.quietus.client.particle.QuietusParticles;
import com.quietus.data.ItemModelProperties.GrapplingHookCast;
import com.quietus.entity.monster.PlayerFragment;
import com.quietus.client.packet.DoDecayPacket;
import com.quietus.client.packet.GhostStatePacket;
import com.quietus.client.packet.GrapplingActiveHookPacket;
import com.quietus.client.packet.GrapplingHookPhysicsPacket;
import com.quietus.client.packet.ManaPacket;
import com.quietus.client.packet.PlayerRevivalCooldownPacket;
import com.quietus.client.packet.SkillTreeAdvancementsGrantRevokePacket;
import com.quietus.client.packet.SkillTreeAdvancementsUpdatePacket;
import com.quietus.client.packet.SkillTreeUpdatePacket;
import com.quietus.client.packet.WeatherItemContainerPacket;

import com.quietus.server.handler.SkillTreeGUIPayloadHandler;
import com.quietus.server.packet.GrapplingJumpReleasePacket;
import com.quietus.server.packet.SkillTreeGUIRequest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.quietus.Quietus.MODID;
import static com.quietus.util.QuietusAttributes.MANA_REGEN_BONUS;
import static com.quietus.util.QuietusAttributes.MAX_MANA;

import com.quietus.client.renderer.BowslingerRenderer;
import com.quietus.client.renderer.ParabolerRenderer;
import com.quietus.entity.QuietusEntityTypes;
import com.quietus.entity.monster.Bowslinger;
import com.quietus.entity.monster.Paraboler;


@EventBusSubscriber(modid = MODID)
public class QuietusIModBusEvent {
    @SubscribeEvent
    public static void PayloadHandlerRegistration(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID)
            .executesOn(HandlerThread.MAIN);
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
        registrar.playToClient(
            GrapplingHookPhysicsPacket.TYPE,
            GrapplingHookPhysicsPacket.STREAM_CODEC,
            ClientPayloadHandler::handleGrapplePhysics
        );
        registrar.playToClient(
            GrapplingActiveHookPacket.TYPE,
            GrapplingActiveHookPacket.STREAM_CODEC,
            ClientPayloadHandler::handleGrappleActivity
        );
        registrar.playToClient(
                SkillTreeUpdatePacket.TYPE,
            SkillTreeUpdatePacket.STREAM_CODEC,
            ClientSkillTreePayloadHandler::handleSkillTreeUpdate
        );
        registrar.playToClient(
            SkillTreeAdvancementsUpdatePacket.TYPE,
            SkillTreeAdvancementsUpdatePacket.STREAM_CODEC,
            ClientSkillTreePayloadHandler::handleSkillTreeAdvancementsSync
        );
        registrar.playToClient(
            SkillTreeAdvancementsGrantRevokePacket.TYPE,
            SkillTreeAdvancementsGrantRevokePacket.STREAM_CODEC,
            ClientSkillTreePayloadHandler::handleSkillTreeAdvancementsSync
        );
        registrar = registrar.executesOn(HandlerThread.NETWORK);
        registrar.playToServer(
            SkillTreeGUIRequest.TYPE,
            SkillTreeGUIRequest.STREAM_CODEC,
            SkillTreeGUIPayloadHandler::handleSkillTreeRequest
        );
        registrar.playToServer(
            GrapplingJumpReleasePacket.TYPE,
            GrapplingJumpReleasePacket.STREAM_CODEC,
            ClientPayloadHandler::handleGrappleJump
        );
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AmethystProjectileModel.LAYER_LOCATION, AmethystProjectileModel::createBodyLayer);
        event.registerLayerDefinition(AmethystProjectileSmallModel.LAYER_LOCATION, AmethystProjectileSmallModel::createBodyLayer);
        event.registerLayerDefinition(ChainHookModel.LAYER_LOCATION, ChainHookModel::createBodyLayer);
    }


    @SubscribeEvent
    public static void registerDecorators(RegisterItemDecorationsEvent event) {
        /* put item decorators here (like durability bar, item count etc.). 
         * The rendering ALWAYS go after vanilla decorators, and the item sprite.
         * That means, whatever item decorators will render on top of the vailla
         * decorators and the item sprite. */
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
        event.put(QuietusEntityTypes.PLAYER_FRAGMENT.get(), PlayerFragment.createAttributes().build());
    }
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(QuietusEntityTypes.BOWSLINGER.get(), BowslingerRenderer::new);
        event.registerEntityRenderer(QuietusEntityTypes.PARABOLER.get(), ParabolerRenderer::new);
        event.registerEntityRenderer(QuietusEntityTypes.PLAYER_FRAGMENT.get(), PlayerFragmentRenderer::new);
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
    

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(QuietusParticles.DUST_EXPLOSION.get(), DustExplosionParticle.Provider::new);
        event.registerSpriteSet(QuietusParticles.DUST_IMPLOSION.get(), DustImplosionParticle.Provider::new);
    }


    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerConditionalProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(
                // The name to reference as the type
                Identifier.fromNamespaceAndPath(MODID, "grappling_hook_cast"),
                // The map codec
                GrapplingHookCast.MAP_CODEC
        );
    }
}
