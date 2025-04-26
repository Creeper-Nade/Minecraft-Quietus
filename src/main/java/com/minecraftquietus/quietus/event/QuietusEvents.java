package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.potion.QuietusPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

import static com.minecraftquietus.quietus.Quietus.MODID;

@EventBusSubscriber(modid=MODID)
public class QuietusEvents {
    @SubscribeEvent
    public static void registerBrewingRecipe(RegisterBrewingRecipesEvent event)
    {
        PotionBrewing.Builder builder=event.getBuilder();

        builder.addMix(Potions.THICK, Items.GLOW_BERRIES, QuietusPotions.SPELUNKER_POTION);
    }
}
