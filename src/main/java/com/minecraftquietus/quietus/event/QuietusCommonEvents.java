package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.core.ManaComponent;
import com.minecraftquietus.quietus.effects.QuietusEffects;
import com.minecraftquietus.quietus.effects.spelunker.Ore_Vision;
import com.minecraftquietus.quietus.item.QuietusComponents;
import com.minecraftquietus.quietus.item.weapons.MultiProjectileBowItem;
import com.minecraftquietus.quietus.potion.QuietusPotions;
import com.minecraftquietus.quietus.util.PlayerData;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import com.minecraftquietus.quietus.util.handler.ClientPayloadHandler;
import com.minecraftquietus.quietus.util.mana.Mana;
import com.minecraftquietus.quietus.util.mana.ManaHudOverlay;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

import static com.minecraftquietus.quietus.Quietus.MODID;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;


@EventBusSubscriber(modid=MODID)
public class QuietusCommonEvents {
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();


    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.RightClickItem event) {
        if (!event.getItemStack().has(QuietusComponents.USES_MANA.get())) { // Item does not use mana
            return;
        } else {
            Player player = event.getEntity();
            ItemStack itemstack = event.getItemStack();
            int mana_consume = itemstack.get(QuietusComponents.USES_MANA.get());
            boolean flag = false;
            if (event.getSide() == LogicalSide.CLIENT) { // Client side
                //LOGGER.info("Expected mana consumption on use: "+mana_consume + " | Player's mana: " + ClientPayloadHandler.getInstance().GetManaFromPack());
                flag = ClientPayloadHandler.getInstance().GetManaFromPack() >= mana_consume;
                if (!flag) {
                    event.setCancellationResult(InteractionResult.FAIL);
                    player.stopUsingItem();
                    event.setCanceled(true);
                } else {
                }
            } else {
                //LOGGER.info("Expected mana consumption on use: "+mana_consume + " | Player's mana: " + Mana.getMana(player));
                flag = Mana.getMana(player) >= mana_consume;
                if (!flag) {
                    event.setCancellationResult(InteractionResult.FAIL);
                    player.stopUsingItem();
                    event.setCanceled(true);}
                else {
                    if (itemstack.getItem().getUseDuration(itemstack, player) == 0 ) {
                        Mana.get(player).consumeMana(mana_consume, player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onUseStop(LivingEntityUseItemEvent.Stop event) {
        ItemStack itemstack = event.getItem();
        LivingEntity entity = event.getEntity();
        if (!itemstack.has(QuietusComponents.USES_MANA.get())) { // Item does not use mana
            return;
        } else {
            int mana_consume = itemstack.get(QuietusComponents.USES_MANA.get());
            if (itemstack.getItem() instanceof BowItem || itemstack.getItem() instanceof MultiProjectileBowItem) {
                Mana.get(entity).consumeMana(mana_consume, entity);
            }
        }
    }

    @SubscribeEvent
    public static void onUseTick(LivingEntityUseItemEvent.Tick event) {
        ItemStack itemstack = event.getItem();
        //LOGGER.info("Tick! "+event.getDuration() + " | " + itemstack.getUseDuration(event.getEntity()));
        LivingEntity entity = event.getEntity();
        if (!itemstack.has(QuietusComponents.USES_MANA.get())) { // Item does not use mana
            return;
        } else {
            int mana_consume = itemstack.get(QuietusComponents.USES_MANA.get());
            if (!entity.level().isClientSide()) { // Server side
                if (Mana.getMana(entity) < mana_consume) {
                    entity.stopUsingItem();
                    event.setCanceled(true);
                }
                if (event.getDuration() == 1) { // About to finish item
                    Mana.get(entity).consumeMana(mana_consume, entity);
                }
            } else {
                if (entity instanceof Player) {
                    if (ClientPayloadHandler.getInstance().GetManaFromPack() < mana_consume) {
                        entity.stopUsingItem();
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            //System.out.println(serverPlayer);
            PlayerData.ManapackToPlayer(serverPlayer);
        } 
    }

    @SubscribeEvent
    public static void registerBrewingRecipe(RegisterBrewingRecipesEvent event)
    {
        PotionBrewing.Builder builder=event.getBuilder();

        builder.addMix(Potions.THICK, Items.GLOW_BERRIES, QuietusPotions.SPELUNKING);
        builder.addMix(QuietusPotions.SPELUNKING, Items.REDSTONE, QuietusPotions.LONG_SPELUNKING); // longer duration of potion of spelunking
        builder.addMix(QuietusPotions.SPELUNKING, Items.GLOW_INK_SAC, QuietusPotions.STRONG_SPELUNKING);
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().is(Tags.Blocks.ORES)) {
            Ore_Vision.RemoveSingleBlock(event);
        }
    }

    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getState().is(Tags.Blocks.ORES)) {
            Ore_Vision.AddSingleBlock(event);
        }

    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null && player.hasEffect(QuietusEffects.SPELUNKING_EFFECT)) {
            Ore_Vision.IfPlayerMoved(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event)
    {
        Player player=event.getEntity();
        //if (event.getEntity().level().isClientSide()) return;
        if(player instanceof ServerPlayer serverPlayer) {
            event.getEntity().getData(QuietusAttachments.MANA_ATTACHMENT).tick(serverPlayer);
            //ManaHudOverlay.SetTick(serverPlayer);
        }
    }



    /*
    public static void onWorldRenderLast(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        PoseStack poseStack = event.getPoseStack();

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }

        if (player.hasEffect(QuietusEffects.SPELUNKING_EFFECT) && player != null) {
            // this is a world pos of the player
            Ore_Vision.updateVisibleOres(player);
            Ore_Vision.renderOreOutlines(poseStack);
        }
        else
        {
            Ore_Vision.clearAllOutlines();
            return;
        }
    }*/
    /*
    @SubscribeEvent
    public static void PayloadHandlerRegistration(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToClient(ManaPack.TYPE, ManaPack.MANA_PACK_STREAM_CODEC, ClientPayloadHandler::ManaHandler);
    }

     */


}
