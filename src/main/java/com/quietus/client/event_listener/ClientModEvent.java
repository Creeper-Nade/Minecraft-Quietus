package com.quietus.client.event_listener;

import com.quietus.client.model.projectile.misc.ChainHookRenderer;
import com.quietus.client.tooltip.ClientMagicWeaponControlsTooltip;
import com.quietus.client.tooltip.ClientTooltipSpacer;
import com.quietus.client.hud.GrapplingHookHudOverlay;
import com.quietus.client.hud.VoidOrreryHudOverlay;
import com.quietus.item.tooltip.MagicWeaponControlsTooltip;
import com.quietus.item.tooltip.TooltipSpacer;
import com.quietus.item.QuietusItems;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;

import static com.quietus.Quietus.MODID;

import org.slf4j.Logger;

import com.quietus.client.QuietusKeyBindings;
import com.quietus.client.model.projectile.magic.AmethystProjectileRenderer;
import com.quietus.client.model.projectile.magic.AmethystProjectileSmallRenderer;
import com.quietus.entity.projectiles.QuietusProjectiles;
import com.mojang.logging.LogUtils;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientModEvent {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final net.minecraft.resources.Identifier GRAPPLING_HOOK_HUD_LAYER =
            net.minecraft.resources.Identifier.fromNamespaceAndPath(MODID, "grappling_hook_hotbar_slot");
    private static final net.minecraft.resources.Identifier VOID_ORRERY_HUD_LAYER =
            net.minecraft.resources.Identifier.fromNamespaceAndPath(MODID, "void_orrery");

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
        event.registerCategory(QuietusKeyBindings.TRANSLATION_KEY_CATEGORY_QUIETUS);
        event.register(QuietusKeyBindings.SKILL_TREE_KEY.get());
        event.register(QuietusKeyBindings.GRAPPLING_HOOK_KEY.get());
    }

    @SubscribeEvent
    public static void onRegisterTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(MagicWeaponControlsTooltip.class, ClientMagicWeaponControlsTooltip::new);
        event.register(TooltipSpacer.class, ClientTooltipSpacer::new);
    }

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        if (!event.getItemStack().is(QuietusItems.AMETHYST_STAFF.get())) {
            return;
        }
        var elements = event.getTooltipElements();
        for (int i = 0; i < elements.size(); i++) {
            var text = elements.get(i).left().orElse(null);
            if (text instanceof Component component
                    && component.getContents() instanceof TranslatableContents translatable
                    && translatable.getKey().equals("tooltip.quietus.amethyst_staff.6")) {
                elements.add(i, Either.right(new TooltipSpacer(3)));
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                GRAPPLING_HOOK_HUD_LAYER,
                GrapplingHookHudOverlay::render
        );
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                VOID_ORRERY_HUD_LAYER,
                VoidOrreryHudOverlay::render
        );
    }
}
