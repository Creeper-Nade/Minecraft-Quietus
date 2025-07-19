package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.client.handler.ClientPayloadHandler;
import com.minecraftquietus.quietus.client.item.DecayBarDecorator;
import com.minecraftquietus.quietus.client.model.projectile.magic.AmethystProjectileModel;
import com.minecraftquietus.quietus.packet.DoDecayPacket;
import com.minecraftquietus.quietus.packet.GhostStatePacket;
import com.minecraftquietus.quietus.packet.ManaPacket;
import com.minecraftquietus.quietus.packet.PlayerRevivalCooldownPacket;
import com.minecraftquietus.quietus.packet.WeatherItemContainerPacket;

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

    @SubscribeEvent // on the mod event bus
    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        // 基准熟肉类设置 (已存在)
        event.modify(Items.COOKED_BEEF, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(512).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.COOKED_MUTTON, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(512).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.COOKED_CHICKEN, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(512).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.COOKED_PORKCHOP, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(512).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.COOKED_RABBIT, builder -> 
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(512).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.COOKED_COD, builder -> 
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(448).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.COOKED_SALMON, builder -> 
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(448).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );

        // ========== 新增食物设置 ==========
        
        // 生肉类食材 (低保鲜度)
        event.modify(Items.BEEF, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(256).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.MUTTON, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(256).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.CHICKEN, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(256).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.PORKCHOP, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(256).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.RABBIT, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(256).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        
        // 生鱼类食材 (比肉类更低)
        event.modify(Items.COD, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(192).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.SALMON, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(192).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        
        // 土豆类
        event.modify(Items.POTATO, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(192).convertsInto(new ItemStack(Items.POISONOUS_POTATO)).build())
        );
        event.modify(Items.BAKED_POTATO, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(384).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        
        // 面包类 (高保鲜度)
        event.modify(Items.BREAD, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(640).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        
        // 曲奇 (极高保鲜度)
        event.modify(Items.COOKIE, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(768).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        
        // 干海带 (最高保鲜度)
        event.modify(Items.DRIED_KELP, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(1024).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        
        // 蛋糕
        event.modify(Items.CAKE, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(800).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        
        // 炖菜类
        event.modify(Items.MUSHROOM_STEW, builder ->  // 蘑菇炖菜
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(512).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.BEETROOT_SOUP, builder ->  // 甜菜汤
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(512).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.RABBIT_STEW, builder ->    // 兔肉煲
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(512).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        
        // 金胡萝卜 (不腐败)
        event.modify(Items.GOLDEN_CARROT, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(Integer.MAX_VALUE).convertsInto(ItemStack.EMPTY).build())
        );
        
        // 其他植物类食材
        event.modify(Items.CARROT, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(320).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.BEETROOT, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(320).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.APPLE, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(384).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.MELON_SLICE, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(256).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.SWEET_BERRIES, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(192).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        event.modify(Items.GLOW_BERRIES, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(192).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
        
        // 毒马铃薯 (不腐败)
        event.modify(Items.POISONOUS_POTATO, builder ->
            builder.remove(QuietusComponents.CAN_DECAY.get())
        );
        /* // Removes the component for any items that have a crafting remainder
        event.modifyMatching(
            item -> !item.getCraftingRemainder().isEmpty(),
            builder -> builder.remove(DataComponents.BUCKET_ENTITY_DATA)
        ); */
    }
}
