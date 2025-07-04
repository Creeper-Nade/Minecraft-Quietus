package com.minecraftquietus.quietus.core.DeathRevamp;

import com.minecraftquietus.quietus.sounds.QuietusSounds;
import com.minecraftquietus.quietus.util.handler.ClientPayloadHandler;
import com.minecraftquietus.quietus.util.sound.EntitySoundSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class GhostDeathScreen{
    private static Component deathMessage;
    private static int reviveCooldown;
    private static int playerScore;
    private static float pulseScale = 1.0f;
    private static int pulseColor = 0x00FF00;
    private static int lastSecond=0;
    private static int animation_cd_tick = 0;
    private static boolean isShowing = false;
    private static boolean animation_flag=false;

    public static void show(Component causeOfDeath) {
        deathMessage = causeOfDeath;
        reviveCooldown = ClientPayloadHandler.getInstance().getMaxReviveCD();
        playerScore = Minecraft.getInstance().player.getScore();
        animation_cd_tick = 0;
        isShowing = true;
        animation_flag=false;
        pulseScale = 1.0f;
        pulseColor = 0x00FF00; // Reset to green
    }


    public static void tick() {
        if(!isShowing|| reviveCooldown<=0||!animation_flag) return;
        if(animation_cd_tick<=20)
        animation_cd_tick++;
        else
        {
            //reset
            animation_flag=false;
            animation_cd_tick=0;
        }
    }
    @SubscribeEvent
    public static void render(RenderGuiEvent.Pre event) {
        if (!ClientPayloadHandler.getInstance().getGhostState()) return;
        GuiGraphics guiGraphics = event.getGuiGraphics();

        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // cd
        int cd = ClientPayloadHandler.getInstance().GetReviveCD();
        int seconds= cd/20;

        // Calculate fade factor (fade in during first second, out during last second)
        float fade = 1.0f;
        if (cd==reviveCooldown) {
            animation_flag=true;
            fade = Math.max(0.0f, animation_cd_tick / 20f);
        } else if(cd==20) {
            animation_flag=true;
            fade = Math.min(1.0f, (20-animation_cd_tick) / 20f);
        }

        if (fade <= 0) return;
        int alpha = (int)(fade * 255);

        // Render "You Died" title
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
        Component title = Component.translatable("deathScreen.title").withStyle(ChatFormatting.BOLD);
        int titleWidth = mc.font.width(title);
        int titleX = (width / 2 - titleWidth) / 2;
        guiGraphics.drawString(mc.font, title, titleX, 30, 0xFFFFFF | (alpha << 24), false);
        guiGraphics.pose().popPose();

        // Render cause of death
        if (deathMessage != null) {
            int deathY = 85;
            int deathX = width / 2 - mc.font.width(deathMessage) / 2;
            guiGraphics.drawString(mc.font, deathMessage, deathX, deathY, 0xFFFFFF | (alpha << 24), false);
        }

        // Render score
        Component scoreText = Component.translatable("deathScreen.score.value",
                Component.literal(Integer.toString(playerScore)).withStyle(ChatFormatting.YELLOW));
        int scoreX = width / 2 - mc.font.width(scoreText) / 2;
        guiGraphics.drawString(mc.font, scoreText, scoreX, 100, 0xFFFFFF | (alpha << 24), false);

        // Render countdown

        // Render countdown - only if there are seconds left
        if (seconds > 0) {
            // Handle pulsing effect for last 3 seconds
            updatePulseEffect(seconds);

            Component countdown = Component.literal(String.valueOf(seconds))
                    .withStyle(ChatFormatting.BOLD);

            guiGraphics.pose().pushPose();
            // Apply pulsing scale
            float scale = 2.0f * pulseScale; // Base size is 3x
            guiGraphics.pose().scale(scale, scale, scale);

            int scaledWidth = (int) (width / scale);
            int scaledHeight = (int) (height / scale)+40;

            int countdownX = (scaledWidth - mc.font.width(countdown)) / 2;
            int countdownY = (int) ((scaledHeight / 2 + Math.max(40,(4-seconds)*40)) / scale);

            guiGraphics.drawString(
                    mc.font,
                    countdown,
                    countdownX,
                    countdownY,
                    pulseColor | (alpha << 24),
                    false
            );

            guiGraphics.pose().popPose();
        }
    }
    private static void updatePulseEffect(int seconds) {
        if (seconds <= 3) {

            if(seconds!=lastSecond)
            {
                LocalPlayer player = Minecraft.getInstance().player;
                player.level().playLocalSound(player, SoundEvents.WARDEN_HEARTBEAT, EntitySoundSource.of(player), 1.0F, 1.0F);
                lastSecond=seconds;
            }

            // Pulse animation - scales between 0.9 and 1.1
            float pulseSpeed = 2.0f; // Speed of pulse
            pulseScale = (0.9f + 0.2f * (0.5f * (1 + Mth.sin((System.currentTimeMillis() % 2000) / 2000f * (float)Math.PI * 2 * pulseSpeed))))*(4-seconds);

            // Color transition from green to red
            float ratio = (4 - seconds) / 3.0f;
            int r = (int)(Mth.lerp(ratio, 0, 255));
            int g = (int)(Mth.lerp(ratio, 255, 0));
            pulseColor = (r << 16) | (g << 8);
        } else {
            pulseScale = 1.0f;
            pulseColor = 0x00FF00; // Green
        }
    }


}
