package com.minecraftquietus.quietus.client.event_listener;

import com.minecraftquietus.quietus.client.model.projectile.misc.ChainHookRenderer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;

import static com.minecraftquietus.quietus.Quietus.MODID;

import org.slf4j.Logger;

import com.minecraftquietus.quietus.client.QuietusKeyBindings;
import com.minecraftquietus.quietus.client.model.projectile.magic.AmethystProjectileRenderer;
import com.minecraftquietus.quietus.client.model.projectile.magic.AmethystProjectileSmallRenderer;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectiles;
import com.mojang.logging.LogUtils;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvent {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        LOGGER.info("HELLO FROM CLIENT SETUP");
        LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        LOGGER.info("    ########    ");
        LOGGER.info("  ##        ##  ");
        LOGGER.info(" #    oo     # ");
        LOGGER.info("#            #");
        LOGGER.info("#   ------   #");
        LOGGER.info(" #  \\__/  # ");
        LOGGER.info("  ##        ##  ");
        LOGGER.info("    ########    ");
        EntityRenderers.register(QuietusProjectiles.AMETHYST_PROJECTILE.get(), AmethystProjectileRenderer::new);
        EntityRenderers.register(QuietusProjectiles.SMALL_AMETHYST_PROJECTILE.get(), AmethystProjectileSmallRenderer::new);
        EntityRenderers.register(QuietusProjectiles.CHAIN_GRAPPLING_HOOK_PROJECTILE.get(), ChainHookRenderer::new);
    }

    @SubscribeEvent
    public static void onKeyMappingRegister(RegisterKeyMappingsEvent event) {
        event.register(QuietusKeyBindings.SKILL_TREE_KEY.get());
    }
}
