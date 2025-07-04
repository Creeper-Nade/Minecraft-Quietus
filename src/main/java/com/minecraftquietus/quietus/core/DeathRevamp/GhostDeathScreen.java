package com.minecraftquietus.quietus.core.DeathRevamp;

import com.minecraftquietus.quietus.util.handler.ClientPayloadHandler;
import com.minecraftquietus.quietus.util.sound.EntitySoundSource;
import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import static com.minecraftquietus.quietus.Quietus.MODID;
@EventBusSubscriber(modid=MODID)
public class GhostDeathScreen{
    private static Component deathMessage;
    private static int reviveCooldown;
    private static int playerScore;
    private static float pulseScale = 1.0f;
    private static int pulseColor = 0x00FF00;
    private static int lastSecond=0;
    private static int animation_cd_tick = 0;
    private static float fade=0;
    private static boolean animation_flag=false;
    private static float IntensityAddition =0.02f;


    public static void show(Component causeOfDeath) {
        deathMessage = causeOfDeath;
        reviveCooldown = ClientPayloadHandler.getInstance().getMaxReviveCD();
        playerScore = Minecraft.getInstance().player.getScore();
        animation_cd_tick = 0;
        animation_flag=true;
        vignetteIntensity=0.8f;
        fade=0;
        IntensityAddition =0.02f;
        pulseScale = 1.0f;
        pulseColor = 0x00FF00; // Reset to green
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)||!animation_flag) return;

        if(animation_cd_tick<=20) {
            animation_cd_tick++;

        }
        else
        {
            //reset
            animation_cd_tick=0;
            animation_flag=false;
        }

    }
    //For countdown ui
    @SubscribeEvent
    public static void render(RenderGuiEvent.Pre event) {
        if (!ClientPayloadHandler.getInstance().getGhostState() && fade <= 0) return;
        GuiGraphics guiGraphics = event.getGuiGraphics();

        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // cd
        int cd = ClientPayloadHandler.getInstance().GetReviveCD();
        int seconds= cd/20;

        // Calculate fade factor (fade in during first second, out during last second)
        fade = 1.0f;
        if (cd==reviveCooldown) {
            animation_flag=true;
            fade = Math.max(0.0f, animation_cd_tick / 20f);
        } else if(cd==0) {
            animation_flag=true;
            fade = Math.min(1.0f, (10-animation_cd_tick) / 10f);
            if(fade<=0)
            {
                animation_cd_tick=0;
                animation_flag=false;
                cleanup();
            }
        }

        if(fade<=0) return;
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
            //vignetteIntensity = 0.8f + (3 - seconds) * 0.2f;
            if(seconds<=1)
                UpdateVignetteLastSecond();
            else
                UpdateVignetteIntensity(0.8f + (4 - seconds) * 0.2f,seconds);
        } else {
            pulseScale = 1.0f;
            pulseColor = 0x00FF00; // Green
            vignetteIntensity=0.8f;
        }
    }


    private static void UpdateVignetteIntensity(float targetIntensity,int seconds)
    {
        if (vignetteIntensity>=targetIntensity)
        {
            if(seconds-1<=1)
            IntensityAddition =0.25f;
            else
                IntensityAddition =0.02f;
            System.out.println(IntensityAddition);
            return;
        }
        vignetteIntensity+= IntensityAddition;

        if(IntensityAddition>=0)
        IntensityAddition -=0.001f;
        else
        IntensityAddition=0;
    }
    private static void UpdateVignetteLastSecond()
    {
        vignetteIntensity+= IntensityAddition;

        IntensityAddition -=0.01f;
    }

    //For vignette shader

    public static RenderPipeline ghostPipeline = RenderPipeline.builder()
            .withLocation(ResourceLocation.fromNamespaceAndPath(MODID, "ghost_effect"))
            .withVertexShader(ResourceLocation.fromNamespaceAndPath(MODID, "core/ghost_effect"))
            .withFragmentShader(ResourceLocation.fromNamespaceAndPath(MODID, "core/ghost_effect"))
            .withSampler("DiffuseSampler")  // Correct sampler declaration
            .withUniform("ScreenSize", UniformType.VEC2)
            .withUniform("VignetteIntensity", UniformType.FLOAT)
            .withUniform("Time", UniformType.FLOAT)  // Add time uniform
            .withUniform("FadeFactor", UniformType.FLOAT)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .build();
    private static float vignetteIntensity = 0.8f;
    private static GpuBuffer vertexBuffer;
    private static int vertexCount = 0;


    private static void createVertexBuffer() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
        }

        // Create normalized device coordinates (NDC) quad
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_TEX
        );

        // Triangle 1 (bottom-left)
        bufferBuilder.addVertex(-1, -1, 0).setUv(0, 0);
        bufferBuilder.addVertex(-1, 1, 0).setUv(0, 1);
        bufferBuilder.addVertex(1, 1, 0).setUv(1, 1);

        // Triangle 2 (top-right)
        bufferBuilder.addVertex(-1, -1, 0).setUv(0, 0);
        bufferBuilder.addVertex(1, 1, 0).setUv(1, 1);
        bufferBuilder.addVertex(1, -1, 0).setUv(1, 0);

        MeshData meshData = bufferBuilder.buildOrThrow();
        vertexCount = meshData.drawState().vertexCount();

        vertexBuffer = RenderSystem.getDevice().createBuffer(
                () -> "Ghost effect vertex buffer",
                BufferType.VERTICES,
                BufferUsage.STATIC_WRITE,
                meshData.vertexBuffer()
        );

        meshData.close();
    }

    @SubscribeEvent
    public static void onRenderLevelLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        if (!ClientPayloadHandler.getInstance().getGhostState()&& fade <= 0) return;

        Minecraft mc = Minecraft.getInstance();

        // Create vertex buffer once
        if (vertexBuffer == null) {
            createVertexBuffer();
        }

        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();

        RenderTarget renderTarget = mc.getMainRenderTarget();
        if (renderTarget.getColorTexture() == null) return;

        // Bind the framebuffer texture to texture unit 0
        //RenderSystem.setShaderTexture(0, renderTarget.getColorTexture());

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(
                        renderTarget.getColorTexture(),
                        OptionalInt.empty(),
                        renderTarget.getDepthTexture(),
                        OptionalDouble.empty()
                )) {

            // Calculate time in seconds
            float time = (System.currentTimeMillis() % 100000) / 1000.0f;
            renderPass.setPipeline(ghostPipeline);

            // Set uniforms - use correct format
            renderPass.setUniform("ScreenSize", (float)width, (float)height);
            renderPass.setUniform("VignetteIntensity", vignetteIntensity);
            renderPass.bindSampler("DiffuseSampler", renderTarget.getColorTexture());
            renderPass.setUniform("Time", time);
            renderPass.setUniform("FadeFactor", fade);

            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.draw(0, vertexCount);
        }
    }

    public static void cleanup() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
        vertexCount = 0;
    }


}
