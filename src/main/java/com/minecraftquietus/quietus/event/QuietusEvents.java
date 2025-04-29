package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.Config;
import com.minecraftquietus.quietus.effects.QuietusEffects;
import com.minecraftquietus.quietus.effects.spelunker.Ore_Vision;
import com.minecraftquietus.quietus.potion.QuietusPotions;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;


import static com.minecraftquietus.quietus.Quietus.MODID;

import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.Logger;


@EventBusSubscriber(modid=MODID)
public class QuietusEvents {
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void registerBrewingRecipe(RegisterBrewingRecipesEvent event)
    {
        PotionBrewing.Builder builder=event.getBuilder();

        builder.addMix(Potions.THICK, Items.GLOW_BERRIES, QuietusPotions.SPELUNKING);
        /*builder.addMix(QuietusPotions.SPELUNKING, Items.REDSTONE, QuietusPotions.LONG_SPELUNKING);*/ // longer duration of potion of spelunking
    }

    // event testing
    /* 
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() && entity.getType() == EntityType.PLAYER) {
            entity.heal(1);
            LOGGER.info("test!" + " " + entity.getType());
        }
    }
    */

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

    public static void onPlayerTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null &&player.hasEffect(QuietusEffects.SPELUNKING_EFFECT)) {
            Ore_Vision.IfPlayerMoved(player);
        }
    }



    public static void onWorldRenderLast(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }

        if (player.hasEffect(QuietusEffects.SPELUNKING_EFFECT) && player != null) {
            // this is a world pos of the player
            Ore_Vision.updateVisibleOres(player);
            Ore_Vision.renderOreOutlines(event);
        }
        else
        {
            Ore_Vision.clearAllOutlines();
            return;
        }
    }

}
