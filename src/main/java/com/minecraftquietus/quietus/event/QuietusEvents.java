package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.potion.QuietusPotions;
import com.mojang.logging.LogUtils;


import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;


import static com.minecraftquietus.quietus.Quietus.MODID;

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

}
